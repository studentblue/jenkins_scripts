#!/bin/bash

time_to_sleep=10s

docker network create my-cloud1

docker run --rm -d --network=my-cloud1 --name cloud-demo1-testa-db-server docker-registry-cpsiot-2018.pii.at/cloud-demo1-testa/my-database:224132_20190307-cl-ver-104

#sr
sleep ${time_to_sleep}
docker run  -d --network=my-cloud1 --name cloud-demo1-testa-service-registry docker-registry-cpsiot-2018.pii.at/cloud-demo1-testa/my-registry:013056_20190308-cl-ver-109

#auth
sleep ${time_to_sleep}
docker run  -d --network=my-cloud1 --name cloud-demo1-testa-authorization docker-registry-cpsiot-2018.pii.at/cloud-demo1-testa/my-authorization:013056_20190308-cl-ver-109

#gateway
#sleep ${time_to_sleep}
#docker run  --rm -d --network=my-cloud1 --name cloud-demo1-testa-gateway docker-registry-cpsiot-2018.pii.at/cloud-demo1-testa/my-gateway:224132_20190307-cl-ver-104

#eventhandler
#sleep ${time_to_sleep}
#docker run  --rm -d --network=my-cloud1 --name cloud-demo1-testa-eventhandler docker-registry-cpsiot-2018.pii.at/cloud-demo1-testa/my-eventhandler:224132_20190307-cl-ver-104

#gatekeeper
#sleep ${time_to_sleep}
#docker run  --rm -d --network=my-cloud1 --name cloud-demo1-testa-gatekeeper docker-registry-cpsiot-2018.pii.at/cloud-demo1-testa/my-gatekeeper:224132_20190307-cl-ver-104

#orchestrator
#sleep ${time_to_sleep}
#docker run  --rm -d --network=my-cloud1 --name cloud-demo1-testa-orchestrator docker-registry-cpsiot-2018.pii.at/cloud-demo1-testa/my-orchestrator:224132_20190307-cl-ver-104
