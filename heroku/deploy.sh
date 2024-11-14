#!/bin/bash

SCRIPT_DIR=$(readlink -f "$0" | xargs dirname)
. "$SCRIPT_DIR"/utils.sh

usage() {
  echo "Usage: $0 -p <AWS_PROFILE>" 1>&2
  exit 1
}

_aws() {
  aws --profile "$AWS_PROFILE" "$@"
}

login() {
  account_id=$(_aws sts get-caller-identity --query "Account" --output text)
  if [ -n "$account_id" ]; then
    log_info "🔑 Already logged in to AWS account $account_id"
  else
    _aws sso login
  fi
}

deploy_services() {
  cluster_arn=$(_aws ecs list-clusters --query 'clusterArns[0]' --output text)

  services=$(_aws resourcegroupstaggingapi get-resources --tag-filters Key=repo,Values="$1" --resource-type-filters ecs:service --query 'ResourceTagMappingList[*].ResourceARN' --output text )
  if [ -z "$services" ]; then
    exit_error "No ECS services found"
  fi

  for service in $services; do
    service_name=$(echo "$service" | awk -F/ '{print $NF}' | awk -F- '{print $(NF - 1)}')
    log_info "🚀 Deploying service $service_name"
    _aws ecs update-service --cluster "$cluster_arn" --service "$service" --force-new-deployment > /dev/null || exit_error "Failed to deploy service $service"
  done

  log_success "✅ Services deployed successfully"
}

unset -v AWS_PROFILE

while getopts "p:h" o; do
    case "${o}" in
        p)
            AWS_PROFILE=${OPTARG}
            ;;
        h|*)
            usage
            ;;
    esac
done
shift $((OPTIND-1))

[ -n "$AWS_PROFILE" ] || exit_error "Missing AWS_PROFILE"

check_command aws

login
deploy_services marketplace-api
