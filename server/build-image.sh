#!/usr/bin/env bash
REGION=${1:-us-east-2}
docker build -t ktrobots-server .
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin "065150860170.dkr.ecr.${REGION}.amazonaws.com"
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin "065150860170.dkr.ecr.${REGION}.amazonaws.com"
docker tag ktrobots-server:latest "065150860170.dkr.ecr.${REGION}.amazonaws.com/ktrobots-server:latest"
docker push "065150860170.dkr.ecr.${REGION}.amazonaws.com/ktrobots-server:latest"
