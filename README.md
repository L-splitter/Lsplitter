# # Large Language Model-Based Decomposition of Long Methods

  - [General Introduction](#General-Introduction)
  - [Contents of the Replication Package](#Contents-of-the-Replication-Package)
  - [Requirements](#Requirements)
  - [Replicate the Evaluation](#How-to-Replicate-the-Evaluation)

 # General Introduction

This is the replication package for ISSTA submission, containing both tool and data that are requested by the replication. It also provides detailed instructions to replicate the evaluation.

  # Contents of the Replication Package

  /Dataset: Benchmark Datasets

  /Lsplitter: The implementation of Lsplitter

  # Requirements

  - Java >= 17

  # How to Replicate the Evaluation?

 ## Replicate the evaluation of Lsplitter In Intellij IDEA

   1. **Import project**

      `Go to *File* -> *open*`

      Browse to the `Lsplitter` directory

      `Click *OK* -> *Trust Project*`
  
   2. **Run the experiment**

       If you want to start the replication from beginning, run `test/java/extract/Lsplitter.java` 
       
       If you want to start the replication based on the existing results of ChatGPT, run `test/java/extract/LsplitterHasGPTAnswer.java`
       
       You need to modify several variables in the code:
       
       - `apiKey` to your ChatGPT API Key.
       - `originalFile` to the path where the original files are located.
       - `gptFile` to the path where the ChatGPT's results are located/saved.
       - `resFile` to the path where the result files should be saved.
       
       ![Example picture](https://cdn.sa.net/2023/12/19/9MBWZk82w4nLo5f.png)
