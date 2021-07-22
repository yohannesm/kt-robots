#!/usr/bin/env bash
is_set() {
    local v=$1
    local message=$2
    if [ ${v+x} ]; then
        echo " = '${v}'"
    else
        echo $message
        exit 1
    fi
}

AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
is_set $AWS_ACCOUNT_ID "Unable to get AWS_ACCOUNT_ID from caller-identity"
VPC_ID=$(aws ec2 describe-vpcs --filters Name=isDefault,Values=true --query 'Vpcs[*].VpcId' --output text)
is_set $VPC_ID "Unable to get default VPC_ID from list of VPCs"
REGION=${1:-us-east-1}
SUBNET_IDS=$(aws ec2 describe-subnets --filter Name=vpc-id,Values=$VPC_ID --query 'Subnets[?MapPublicIpOnLaunch==`true`].SubnetId' --output text | sed 's/\t/,/g')
is_set $SUBNET_IDS "Unable to find public subnets for default VPC"

aws cloudformation deploy --stack-name kt-robots-server --template-file fargate_cluster_cfn.yml --parameter-overrides VpcId=${VPC_ID}  --capabilities CAPABILITY_NAMED_IAM
echo "Building Docker image"
docker build -t ktrobots-server .

echo "Loggin in"
aws ecr get-login-password --region "${REGION}" | docker login --username AWS --password-stdin "${AWS_ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com"
echo "Tagging image"
docker tag ktrobots-server:latest "${AWS_ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com/kt-robots-server-repository:latest"

echo "Pushing image"
docker push "${AWS_ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com/kt-robots-server-repository:latest"

aws cloudformation deploy --stack-name kt-robots-server-service --template-file fargate_service_cfn.yml --parameter-overrides VpcId=${VPC_ID} Subnets=${SUBNET_IDS} Cpu=1024 Memory=2GB  --capabilities CAPABILITY_NAMED_IAM

ECS_TASK_ARN=$(aws ecs list-tasks --cluster kt-robots-server-cluster --query 'taskArns[*]' --output text | sed 's/\t/,/g')
