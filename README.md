

# AI Powered Chatbot Setup Steps



## 1. Oracle 23ai setup details

    # run oracle as docker container by running the below.
    docker run -d --name 23ai \  
    -p 1521:1521 \  
    -e ORACLE_PWD="welcome1" \  
    container-registry.oracle.com/database/free:latest  
      
    docker ps -a
    mkdir mymodel && cd mymodel


### Download sentense transformer model & unzip  ###

    wget https://adwc4pm.objectstorage.us-ashburn-1.oci.customer-oci.com/p/VBRD9P8ZFWkKvnfhrWxkpPe8K03-JIoM5h_8EJyJcpE80c108fuUjg7R5L5O7mMZ/n/adwc4pm/b/OML-Resources/o/all_MiniLM_L12_v2_augmented.zip
    unzip -oq all_MiniLM_L12_v2_augmented.zip  


### Copy the model to the docker container

    docker cp all_MiniLM_L12_v2.onnx 23ai:/home/oracle/ 

Enter docker container

### Open the interactive shell session inside the oracle docker container and copy the model to required directory

    docker exec -it 23ai bash  
    mkdir mymodel  
    mv all_MiniLM_L12_v2.onnx mymodel      


### Upload the model to the oracle database

    sqlplus sys/welcome1@localhost:1521/FREEPDB1 as sysdba  
    alter session set "_ORACLE_SCRIPT"=true;    
    create or replace directory model_dir as '/home/oracle/mymodel';  
    grant read, write on directory model_dir to sys;  
      
    begin  
    dbms_vector.drop_onnx_model (  
    model_name => 'ALL_MINILM_L12_V2',  
    force => true);  
      
    dbms_vector.load_onnx_model (  
    directory  => 'model_dir',  
    file_name  => 'all_MiniLM_L12_v2.onnx',  
    model_name => 'ALL_MINILM_L12_V2');  
    end;  
    /  

## 2. Ollama Setup

Refer ollama documentation to setup ollama.
https://ollama.com/download

### Pull the required models
Once the ollama setup is completed, open a shell/cmd prompt and run the below to pull required models, you can tweak the code to replace the modeals of your choice.

    ollama pull mistral
    ollama list

### Output

| **NAME**            | **ID**           | **SIZE**   | **MODIFIED**   |
|-----------------|--------------|--------|------------|
| llama3.2:latest | a80c4f17acd5 | 2.0 GB | 5 days ago |
| mistral:latest  | f974a74358d6 | 4.1 GB | 5 days ago |
|                 |              |        |            |

## 3. Pull the code and Run

### Update any hostname/ip in the application.yaml after cloning the application with appropriate hostname/ip.

    git clone git@github.com:bhaskaro/chatwithdocs.git
    ./mvnw spring-boot:run

Once the application is up, you can access it with [http://localhost:8080](http://localhost:8080/chat)
