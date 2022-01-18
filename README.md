# SHACL and ShEx in the Wild❗

## A Community Survey on Validating Shapes Generation and Adoption

Knowledge Graphs (KGs) are the de-facto standard to represent heterogeneous domain knowledge on the Web and within organizations. 
Various tools and approaches exist to manage KGs and ensure the quality of their data.
Among these, the Shapes Constraint Language (SHACL) and the Shapes Expression Language (ShEx) are the two state-of-the-art languages to define validating shapes for KGs.
In the last few years, the usage of these constraint languages has increased, and hence new needs arose.
One such need is to enable the efficient generation of these shapes for existing KGs.
Yet, since these languages are relatively new, we witness a lack of understanding of how they are effectively put to use for existing KGs. 
Therefore, in this poster, we answer **How validating shapes (SHACL/ShEx) are being generated and adopted?**
We conducted a community online survey to analyze the needs of users (both from industry and academia) generating validating shapes.
_Results show the need for developing methods that can automatically generate shapes from large KGs with quality guarantees._


## Datasets
We have used DBpedia and YAGO-4 datasets to extract their SHACL shapes. Details on how we downloaded are given below:

1. **DBPedia:** We used  [dbpedia script](https://github.com/Kashif-Rabbani/validatingshapes/blob/main/dbpedia/download-dbpedia.sh) to download all the dbpedia files listed [here](https://github.com/Kashif-Rabbani/validatingshapes/blob/main/dbpedia/dbpedia-files.txt).
2. **YAGO-4:** We downloaded YAGO-4 English version from [https://yago-knowledge.org/data/yago4/en/](https://yago-knowledge.org/data/yago4/en/).

We provide the statistics of these datasets in the table below:

|                                	| DBpedia 	| YAGO-4 	|
|--------------------------------	|--------:	|-------:	|
| # of triples                   	|    52 M 	|  210 M 	|
| # of distinct objects          	|    19 M 	|  126 M 	|
| # of distinct subjects         	|    15 M 	|    5 M 	|
| # of distinct literals         	|    28 M 	|  111 M 	|
| # of distinct RDF type triples 	|     5 M 	|   17 M 	|
| # of distinct classes          	|     427 	|  8,902 	|
| # of distinct properties       	|   1,323 	|    153 	|
| Size in GBs                    	|     6.6 	|  28.59 	|

You can download a copy of these datasets from our [single archive](http://130.226.98.152/www_datasets/)

### Tools and Approaches

#### 1. SheXer
ABC

#### 2. ShapeDesigner
ABC

#### 3. SHACLGEN
ABC


#### Persistent URI & Licence:
All of the data and results presented in our experimental study are available at
[https://github.com/Kashif-Rabbani/validatingshapes](https://github.com/Kashif-Rabbani/validatingshapes) under [Apache License 2.0](https://github.com/Kashif-Rabbani/validatingshapes/blob/main/LICENSE) .



