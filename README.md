# Indy sidecar

## build image

`./mvnw clean package -Dquarkus.container-image.build=true`

use `src/main/resources/application.properties` to control image build plugin, by default, indy-sidecar use docker as builder.