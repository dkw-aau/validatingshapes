# Shapes Extraction


## Reproducibility Instructions

The following instructions are for reproducing the experiments we presented in our paper. To reproduce and extend the experiments you should clone the branch `master` on the Github repository as follows:

```
git clone https://github.com/Kashif-Rabbani/validatingshapes.git
```
The repository contains all code, and instructions. Dataset should be downloaded separately as explained below.


## Getting the data
We have used the following datasets:

1. **DBPedia:** We used  [dbpedia script](https://github.com/Kashif-Rabbani/validatingshapes/blob/main/dbpedia/download-dbpedia.sh) to download all the dbpedia files listed [here](https://github.com/Kashif-Rabbani/validatingshapes/blob/main/dbpedia/dbpedia-files.txt).
2. **YAGO-4:** We downloaded YAGO-4 English version from [https://yago-knowledge.org/data/yago4/en/](https://yago-knowledge.org/data/yago4/en/).
2. **LUBM:** We generated LUBM dataset following the guidelines available at [LUBM's official Website](http://swat.cse.lehigh.edu/projects/lubm/).


We provide a copy of all our datasets in a [single archive](http://130.226.98.152/www_datasets/). You can download these datasets in `data` folder, and check the size and number of lines (triples) with the following commands:

```
 cd data 
 du -sh yago.n3 or dbpedia.n3 or yago.n3
 wc -l yago.n3 or dbpedia.n3 or yago.n3
```
## Software Setup and Experiments (with Docker)

Note: You will have to update the [config](https://github.com/Kashif-Rabbani/validatingshapes/blob/main/code/shacl/config.properties) file for each dataset accordingly.

#### Build Docker 

Go inside the project directory and execute the following command to build the docker

```
cd code/shacl/
docker build . -t shacl:v1
```

#### Run the container
Running the build image as a container to run QSE approach for LUBM dataset using `config.properties`. 

```
docker run -d --name shacl \
    -m 20GB \
    -e "JAVA_TOOL_OPTIONS=-Xms16g -Xmx16g" \
    --mount type=bind,source=/srv/data/home/data/,target=/app/data \ 
    --mount type=bind,source=/srv/data/home/git/shacl/,target=/app/local \ 
    shacl:v1 /app/local/config.properties
```

`-m` limits the container memory.  <br /> 
`JAVA_TOOL_OPTIONS` specifies the min (`Xms`) and max (`Xmx`) memory values for JVM memory allocation pool. <br />
`-e` sets environment variables. <br />
`-d` runs container in background and prints container ID. <br />
`--name`  assigns a name to the container. <br />
`--mount` attaches a filesystem mount to the container. <br />

#### Get inside the container
```
sudo docker exec -it shacl /bin/sh
```

#### Log Output
```
docker logs --follow shacl
```

#### See Memory Utilization by Docker Container
```
docker stats
```



## Software Setup and Experiments (without Docker)

1. Install Java
   Follow [these](https://sdkman.io/install) steps to install sdkman and execute the following commands to install the specified version of Java/Gradle, build the project, create the Jar, and finally run the jar.

        sdk list java
        sdk install java 17.0.2-open 
        sdk use java java 17.0.2-open 
        

2. Install gradle

        sdk list gradle
        sdk install gradle Gradle 7.4-rc-1
        sdk use gradle Gradle 7.4-rc-1


3. Build project

        gradle clean
        gradle build
        gradle shadowJar
4. Run the jar file by passing the config file as a parameter: `java -jar shacl.jar config.properties`


### Extracted Output Shapes
You can inspect the extracted shapes saved in the Output directory. You will see two files, one in pretty formatting and one in default formatting.  