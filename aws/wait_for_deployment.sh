#!/bin/bash

SCRIPT_DIR=$(readlink -f "$0" | xargs dirname)
. "$SCRIPT_DIR"/utils.sh

unset -v ENV
unset -v BRANCH
COMMIT=HEAD

usage() {
  echo "Usage: $0 [ -e ENV ] [ -c COMMIT ] [-h]"
  echo "  -e ENV: develop, staging, production"
  echo "  -c COMMIT: commit ref (default: HEAD)"
  echo "  -h: show usage"
  echo ""
}

check_args() {
    if [[ -z $ENV ]]; then
      exit_error "❌ Invalid arguments, you must specify an environment"
    fi
}

spinner() {
  ticks=$1
  count=0
  spin='-\|/'

  while [ $count -lt "$ticks" ]; do
    sleep 0.1
    (( count++ ))
    ((i = count % 4))
    printf "\r${spin:$i:1}"
  done
}

check_ci() {
  run=$(gh api "repos/$REPO_OWNER_NAME/actions/runs?head_sha=$SHA&branch=$BRANCH" | jq '.workflow_runs[] | select(.name == "Backend CI")')

  id=$(echo "$run" | jq -r '.id')
  status=$(echo "$run" | jq -r '.conclusion')

  printf "   Backend CI\r"
  case $status in
    success)
      echo "✅"
      return 0
      ;;
    failure)
      echo "❌"
      if ask "CI has failed, do you want to re-run it"; then
        log_info "Re-running id $id"
        gh api -X POST "repos/$REPO_OWNER_NAME/actions/runs/$id/rerun"
        return 1
      fi
      exit_error "☠️  Aborting"
      ;;
    *)
      return 1
      ;;
  esac
}

check_cd() {
  id=$(gh api "/repos/$REPO_OWNER_NAME/deployments?sha=$SHA&environment=$ENV" | jq -r '.[0].id')

  status=
  if [ "$id" != "null" ]; then
    status=$(gh api "/repos/$REPO_OWNER_NAME/deployments/$id/statuses" | jq -r '.[0].state')
  fi

  printf "   Backend CD\r"
  case $status in
    success)
      echo "✅"
      return 0
      ;;
    failure)
      echo "❌"
      if ask "CD has failed, please re-run it manually. Keep waiting"; then
        return 1
      fi
      exit_error "☠️  Aborting"
      ;;
    *)
      return 1
      ;;
  esac
}

check_deployment() {
  actuator_url=$([ "$ENV" = "production" ] && echo "https://api.onlydust.com/actuator/info"|| echo "https://$ENV-api.onlydust.com/actuator/info")
  deployed_sha=$(curl --silent "$actuator_url" | jq -r .git.commit.id)

  printf "   AWS deployment\r"
  if [ "$deployed_sha" == "$SHORT_SHA" ]; then
    echo "🚀"
    return 0
  else
    return 1
  fi
}

wait_for() {
  while ! "$*"; do
    spinner 20
    printf "\b"
  done
}

while getopts "e:c:h" o; do
    case "${o}" in
        e)
            ENV=${OPTARG}
            ((ENV == "develop" || ENV == "staging" || ENV == "production")) || usage
            BRANCH=$([ "$ENV" = "develop" ] && echo main || echo "$ENV")
            ;;
        c)
            COMMIT=${OPTARG}
            ;;
        h|*)
            usage
            ;;
    esac
done
shift $((OPTIND-1))

REPO_OWNER_NAME=$(git remote | head -1 | xargs git remote get-url | sed -E 's;.*[:/]([^:/]+)/([^/]+)\.git$;\1/\2;')
SHA=$(git --no-pager rev-parse "$COMMIT")
SHORT_SHA=$(echo "$SHA" | cut -c-7)

check_commands gh
check_args

#wait_for check_ci
wait_for check_cd
wait_for check_deployment
