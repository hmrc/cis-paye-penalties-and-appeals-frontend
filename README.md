# cis-paye-penalties-and-appeals-frontend

This is the frontend service responsible for the submission of appeals for CIS & PAYE.

For more information please refer to the [documentation](https://confluence.tools.tax.service.gov.uk/spaces/RBD/pages/1389331265/14.+Penalities+and+Appeals+PAS).

## Running the service
Before starting, you will need to have  [service-manager](https://github.com/hmrc/service-manager) installed/configured

### Dependencies
All dependencies can be found in [AppDependencies.scala](https://github.com/hmrc/cis-paye-penalties-and-appeals-frontend/blob/main/project/AppDependencies.scala)

### Running locally:
Service Manager:
- Start dependent services `sm2 --start PAS_ALL`
- Stop this service `sm2 --stop CIS_PAYE_PENALTIES_AND_APPEALS_FRONTEND`
- Start the server locally on `port 61002` with `sbt run`

### Testing:
- Run unit tests: `sbt test`
- Run integration tests: `sbt it/test`
- To run all tests and coverage: `sbt clean compile coverage test it/test coverageOff coverageReport`
- To run the service in test-only mode: `sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes`


### License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").