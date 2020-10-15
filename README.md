# ASAP-GUI
This project is a web based graphical user interface for asynchronous semantic Ad-hoc protocol  (ASAP) application, which was initiated by Thomas Schwotzer.

## API-Demo
This demo was generated with [SpringBoot]

### Run

Run class `DemoApplication` to start the spring boot application. By default the server will run on `http://localhost:8080/`

### Available HTTP-Request
- Create new peer : POST-Request. Example: `localhost:8080/api/v1/asap/peer?name=Alice`
- Create new app : POST-Request. Example: `localhost:8080/api/v1/asap/app?peer=Alice&app=chat`

