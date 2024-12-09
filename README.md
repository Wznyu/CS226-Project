# GameScout

## Group Name: 
   Cohesion_Power

## Team Member:

- Cris Liao: eliao014@ucr.edu 
SID: 862396679

- Zhenyu Wu: wzhen033@ucr.edu 
SID: 862150147

- Wesley He: rhe032@ucr.edu 
SID: 862372351
   
- Stephen Huang: xhuan186@ucr.edu 
SID: 862372587

## Compile and run 
### Unzip file and open in Intellij
### Run in Intellij
1. create a new application

   ![image](https://github.com/user-attachments/assets/55566ba6-ac3b-494a-a07d-5ba9efa708f8)
   ![image](https://github.com/user-attachments/assets/7085eb0c-1959-421c-99c3-eea5c642053b)
2. choose the main function in Datapreparation.
3. choose java version 17
4. click "Modify options" dropdown menu and choose "Add VM options"
5. paste this into the new existing "VM option" box
```
--add-opens
java.base/java.nio=ALL-UNNAMED
--add-opens
java.base/java.util=ALL-UNNAMED
--add-opens
java.base/java.lang=ALL-UNNAMED
--add-opens
java.base/sun.nio.ch=ALL-UNNAMED
--add-opens
java.base/java.lang.invoke=ALL-UNNAMED
-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector

```
Your application should look like this
<img width="802" alt="Screenshot 2024-12-07 at 10 59 16 PM" src="https://github.com/user-attachments/assets/5c2d4c8c-6aad-487b-a17e-8dfe44e84f47">

6. Click "OK" to save this application and then you can run the program for website part.

   ![image](https://github.com/user-attachments/assets/d423a60b-ca5d-4746-b195-e6efe0ba52d4)
7. Repeat the same steps 1-6 for application "Sentiment" to build model. Note: Building model can cost 10 to 30 minutes depending on your computer.
8. Repeat the same steps 1-6 for application "GameScoutApplication" to start website on "http://localhost:8080".

