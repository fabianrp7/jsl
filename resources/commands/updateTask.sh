#!/usr/bin/env bash
# more bash-friendly output for jq
JQ="jq --raw-output --exit-status"

ENVIRONMENT=$1
APP_NAME=$2
CLUSTER_NAME=$3
PROFILE=$4
TEAM_NAME=$5
VERSION=latest
SERVICE_NAME="$ENVIRONMENT-$TEAM_NAME-$APP_NAME-services"
TASK_FAMILY="$ENVIRONMENT-$TEAM_NAME-$APP_NAME-task-family"
IMAGE_TAG=1


configure_aws_cli(){
	aws --version
	aws configure set default.region us-east-1
	aws configure set default.output json
	echo SERVICE_NAME: $SERVICE_NAME
	echo IMAGE_TAG: $IMAGE_TAG
	echo TASK_FAMILY: $TASK_FAMILY
	echo PROFILE: $PROFILE
}

configure_aws_cli