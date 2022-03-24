package cs.qse;

import cs.Main;
import cs.utils.ConfigManager;
import cs.utils.Constants;
import cs.utils.Tuple3;
import cs.utils.Utils;
import cs.utils.Encoder;
import de.atextor.turtle.formatter.FormattingStyle;
import de.atextor.turtle.formatter.TurtleFormatter;
import org.apache.commons.io.FilenameUtils;
import org.apache.jena.riot.RDFDataMgr;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.model.vocabulary.VOID;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.eclipse.rdf4j.model.util.Values.bnode;

/**
 * This class is used to extract/construct shapes using all the information/metadata collected in Parser
 */
public class ShapesExtractor {
    Model model = null;
    ModelBuilder builder;
    Encoder encoder;
    Map<Tuple3<Integer, Integer, Integer>, EntityCount> shapeTripletStats;
    Map<Integer, Integer> classInstanceCount;
    ValueFactory factory = SimpleValueFactory.getInstance();
    IRI reificationIRI;
    
    public ShapesExtractor(Encoder encoder, Map<Tuple3<Integer, Integer, Integer>, EntityCount> shapeTripletStats, Map<Integer, Integer> classInstanceCount) {
        this.encoder = encoder;
        this.builder = new ModelBuilder();
        this.shapeTripletStats = shapeTripletStats;
        this.classInstanceCount = classInstanceCount;
        builder.setNamespace("shape", Constants.SHAPES_NAMESPACE);
    }
    
    public void initiateShapesConstruction(Map<Integer, Map<Integer, Set<Integer>>> classToPropWithObjTypes) {
        this.model = null;
        this.builder = new ModelBuilder();
        this.model = builder.build();
        this.model.addAll(constructShapes(classToPropWithObjTypes));
        this.writeModelToFileWithPrettyFormatting("");
    }
    
    
    private Model constructShapes(Map<Integer, Map<Integer, Set<Integer>>> classToPropWithObjTypes) {
        Model m = null;
        ModelBuilder b = new ModelBuilder();
        classToPropWithObjTypes.forEach((encodedClassIRI, propToObjectType) -> {
            if (Utils.isValidIRI(encoder.decode(encodedClassIRI))) {
                IRI subj = factory.createIRI(encoder.decode(encodedClassIRI));
                String nodeShape = "shape:" + subj.getLocalName() + "Shape";
                b.subject(nodeShape)
                        .add(RDF.TYPE, SHACL.NODE_SHAPE)
                        .add(SHACL.TARGET_CLASS, subj)
                        .add(VOID.ENTITIES, classInstanceCount.get(encodedClassIRI))
                        //.add(SHACL.IGNORED_PROPERTIES, RDF.TYPE)
                        .add(SHACL.CLOSED, false);
                
                if (propToObjectType != null) {
                    constructNodePropertyShapes(b, subj, encodedClassIRI, nodeShape, propToObjectType);
                }
            } else {
                System.out.println("constructShapes::INVALID SUBJECT IRI: " + encoder.decode(encodedClassIRI));
            }
        });
        m = b.build();
        return m;
    }
    
    private void constructNodePropertyShapes(ModelBuilder b, IRI subj, Integer subjEncoded, String nodeShape, Map<Integer, Set<Integer>> propToObjectTypesLocal) {
        
        propToObjectTypesLocal.forEach((prop, propObjectTypes) -> {
            IRI property = factory.createIRI(encoder.decode(prop));
            
            IRI propShape = factory.createIRI("sh:" + property.getLocalName() + subj.getLocalName() + "ShapeProperty");
            reificationIRI = factory.createIRI(Constants.SHAPES_NAMESPACE + subj.getLocalName() + "/" + property.getLocalName());
            b.subject(nodeShape).add(SHACL.PROPERTY, propShape);
            b.subject(propShape).add(RDF.TYPE, SHACL.PROPERTY_SHAPE).add(SHACL.PATH, property);
            
            int numberOfObjectTypes = propObjectTypes.size();
            
            if (numberOfObjectTypes == 1) {
                int encodedObjectType = propObjectTypes.iterator().next();
                String objectType = encoder.decode(encodedObjectType);
                
                //Adding Min Cardinality Constraints
                Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3<>(encoder.encode(subj.stringValue()), prop, encodedObjectType);
                if (shapeTripletStats.containsKey(tuple3)) {
                    if (shapeTripletStats.get(tuple3).getEntityCount().equals(classInstanceCount.get(encoder.encode(subj.stringValue())))) {
                        b.subject(propShape).add(SHACL.MIN_COUNT, 1);
                    }
                }
                // Adding other constraints
                if (objectType != null) {
                    if (objectType.contains(XSD.NAMESPACE) || objectType.contains(RDF.LANGSTRING.toString())) {
                        if (objectType.contains("<")) {objectType = objectType.replace("<", "").replace(">", "");}
                        IRI objectTypeIri = factory.createIRI(objectType);
                        b.subject(propShape).add(SHACL.DATATYPE, objectTypeIri);
                        b.subject(propShape).add(SHACL.NODE_KIND, SHACL.LITERAL);
                    } else {
                        if (Utils.isValidIRI(objectType)) {
                            IRI objectTypeIri = factory.createIRI(objectType);
                            b.subject(propShape).add(SHACL.CLASS, objectTypeIri);
                            b.subject(propShape).add(SHACL.NODE_KIND, SHACL.IRI);
                            
                        } else {
                            System.out.println("INVALID Object Type IRI: " + objectType);
                            b.subject(propShape).add(SHACL.NODE_KIND, SHACL.IRI);
                        }
                    }
                } else {
                    // in case the type is null, we set it default as string
                    b.subject(propShape).add(SHACL.DATATYPE, XSD.STRING);
                }
            }
            
            if (numberOfObjectTypes > 1) {
                List<Resource> members = new ArrayList<>();
                Resource headMember = bnode();
                ModelBuilder localBuilder = new ModelBuilder();
                
                for (Integer encodedObjectType : propObjectTypes) {
                    Tuple3<Integer, Integer, Integer> tuple3 = new Tuple3<>(encoder.encode(subj.stringValue()), prop, encodedObjectType);
                    String objectType = encoder.decode(encodedObjectType);
                    Resource currentMember = bnode();
                    //Cardinality Constraints
                    if (shapeTripletStats.containsKey(tuple3)) {
                        if (shapeTripletStats.get(tuple3).getEntityCount().equals(classInstanceCount.get(encoder.encode(subj.stringValue())))) {
                            b.subject(propShape).add(SHACL.MIN_COUNT, 1);
                        }
                        /*if (Main.extractMaxCardConstraints) {
                            if (propWithClassesHavingMaxCountOne.containsKey(prop) && propWithClassesHavingMaxCountOne.get(prop).contains(subjEncoded)) {
                                b.subject(propShape).add(SHACL.MAX_COUNT, 1);
                            }
                        }*/
                    }
                    
                    if (objectType != null) {
                        if (objectType.contains(XSD.NAMESPACE) || objectType.contains(RDF.LANGSTRING.toString())) {
                            if (objectType.contains("<")) {objectType = objectType.replace("<", "").replace(">", "");}
                            IRI objectTypeIri = factory.createIRI(objectType);
                            
                            localBuilder.subject(currentMember).add(SHACL.DATATYPE, objectTypeIri);
                            localBuilder.subject(currentMember).add(SHACL.NODE_KIND, SHACL.LITERAL);
                            
                            if (shapeTripletStats.containsKey(tuple3)) {
                                Literal entities = Values.literal(shapeTripletStats.get(tuple3).getEntityCount());
                                localBuilder.subject(currentMember).add(VOID.ENTITIES, entities);
                            }
                            
                        } else {
                            if (Utils.isValidIRI(objectType)) {
                                IRI objectTypeIri = factory.createIRI(objectType);
                                localBuilder.subject(currentMember).add(SHACL.CLASS, objectTypeIri);
                                localBuilder.subject(currentMember).add(SHACL.NODE_KIND, SHACL.IRI);
                                if (shapeTripletStats.containsKey(tuple3)) {
                                    Literal entities = Values.literal(shapeTripletStats.get(tuple3).getEntityCount());
                                    localBuilder.subject(currentMember).add(VOID.ENTITIES, entities);
                                }
                            } else {
                                System.out.println("INVALID Object Type IRI: " + objectType);
                                localBuilder.subject(currentMember).add(SHACL.NODE_KIND, SHACL.IRI);
                            }
                        }
                    } else {
                        // in case the type is null, we set it default as string
                        //b.subject(propShape).add(SHACL.DATATYPE, XSD.STRING);
                        localBuilder.subject(currentMember).add(SHACL.DATATYPE, XSD.STRING);
                    }
                    members.add(currentMember);
                }
                Model localModel = RDFCollections.asRDF(members, headMember, new LinkedHashModel());
                localModel.add(propShape, SHACL.OR, headMember);
                localModel.addAll(localBuilder.build());
                b.build().addAll(localModel);
            }
        });
    }
    
    public void writeModelToFileWithPrettyFormatting(String fileIdentifier) {
        Path path = Paths.get(Main.datasetPath);
        String fileName = FilenameUtils.removeExtension(path.getFileName().toString()) + "_SHACL.ttl";
        String fileAddress = ConfigManager.getProperty("output_file_path") + fileName;
        System.out.println("::: SHACLER ~ WRITING MODEL TO FILE: " + fileName);
        try {
            FileWriter fileWriter = new FileWriter(fileAddress, false);
            Rio.write(model, fileWriter, RDFFormat.TURTLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String prettyFileAddress = ConfigManager.getProperty("output_file_path") + FilenameUtils.removeExtension(path.getFileName().toString()) + "_SHACL_PRETTY.ttl";
        TurtleFormatter formatter = new TurtleFormatter(FormattingStyle.DEFAULT);
        // Build or load a Jena Model
        org.apache.jena.rdf.model.Model model = RDFDataMgr.loadModel(fileAddress);
        // Either create a string...
        String prettyPrintedModel = formatter.apply(model);
        // ...or write directly to an OutputStream
        //formatter.accept(model, System.out);
        
        try {
            formatter.accept(model, new FileOutputStream(prettyFileAddress));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //System.out.println(prettyPrintedModel);
        
        
    }
}