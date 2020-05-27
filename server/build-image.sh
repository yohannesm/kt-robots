#!/usr/bin/env bash
AWS_ACCOUNT_ID=${1}
REGION=${2:-us-east-2}
docker build -t ktrobots-server .
aws ecr get-login-password --region "${REGION}" | docker login --username AWS --password-stdin "${AWS_ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com"
aws ecr get-login-password --region "${REGION}" | docker login --username AWS --password-stdin "${AWS_ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com"
docker tag ktrobots-server:latest "${AWS_ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com/ktrobots-server:latest"
docker push "${AWS_ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com/ktrobots-server:latest"
