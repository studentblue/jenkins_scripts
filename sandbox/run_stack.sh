#!/bin/bash

time_to_sleep=10s

docker network create my-cloud1

docker run --rm -d --network=my-cloud1 --name cloud-demo1-testa-db-server docker-registry-cpsiot-2018.pii.at/cloud-demo1-testa/my-database:025743_20190308-cl-ver-117

#sr
sleep ${time_to_sleep}
docker run  -d --network=my-cloud1 --name cloud-demo1-testa-service-registry docker-registry-cpsiot-2018.pii.at/cloud-demo1-testa/my-registry:025743_20190308-cl-ver-117

#auth
sleep ${time_to_sleep}
docker run  -d --network=my-cloud1 --name cloud-demo1-testa-authorization docker-registry-cpsiot-2018.pii.at/cloud-demo1-testa/my-authorization:025743_20190308-cl-ver-117

#gateway
sleep ${time_to_sleep}
docker run  --rm -d --network=my-cloud1 --name cloud-demo1-testa-gateway docker-registry-cpsiot-2018.pii.at/cloud-demo1-testa/my-gateway:025743_20190308-cl-ver-117

#eventhandler
sleep ${time_to_sleep}
docker run  --rm -d --network=my-cloud1 --name cloud-demo1-testa-eventhandler docker-registry-cpsiot-2018.pii.at/cloud-demo1-testa/my-eventhandler:025743_20190308-cl-ver-117

#gatekeeper
sleep ${time_to_sleep}
docker run  --rm -d --network=my-cloud1 --name cloud-demo1-testa-gatekeeper docker-registry-cpsiot-2018.pii.at/cloud-demo1-testa/my-gatekeeper:025743_20190308-cl-ver-117

#orchestrator
sleep ${time_to_sleep}
docker run  --rm -d --network=my-cloud1 --name cloud-demo1-testa-orchestrator docker-registry-cpsiot-2018.pii.at/cloud-demo1-testa/my-orchestrator:025743_20190308-cl-ver-117
