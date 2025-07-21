### Build and run
Application can be built as regular maven project    
`mvn clean package`  
There is dockerfile in the repository so you can create docker image of this application.
### API
API is documented with Swagger. Documentation is available on http://localhost:8080/swagger-ui/index.html

---

The main branch is 'web' - the web version of logs analyzer. The other branch is CLI version of the application
that works in terminal.  
The purpose of the application is to parse NGINX logs and gather statistics on them. CLI version can create
log reports in .adoc and .md formats.