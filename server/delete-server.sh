#!/usr/bin/env bash

aws ecr batch-delete-image --repository-name kt-robots-server-repository --image-ids imageTag=latest

aws cloudformation delete-stack --stack-name kt-robots-server-service
aws cloudformation wait stack-delete-complete --stack-name kt-robots-server-service

aws cloudformation delete-stack --stack-name kt-robots-server
aws cloudformation wait stack-delete-complete --stack-name kt-robots-server
