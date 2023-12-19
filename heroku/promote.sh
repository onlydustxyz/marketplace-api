#!/bin/bash

SCRIPT_DIR=$(readlink -f "$0" | xargs dirname)
. "$SCRIPT_DIR"/utils.sh

FROM_ENV=
FROM_BRANCH=
TO_ENV=
TO_BRANCH=
HEALTH_CHECK_URLS=()
FORCE=0
SKIP_DB_DUMP=0

REMOTE=promote-origin

APPS=(new-api back-office-api)
GIT_REPO_URL=https://github.com/onlydustxyz/marketplace-api.git
ENV_FILES="**/application.yaml Procfile*"

check_args() {
    if [[ -z $FROM_ENV || -z $TO_ENV ]]; then
      exit_error "Invalid arguments, you must specify at least --staging or --production flag"
    fi
}

check_cwd() {
    if ! root_dir=$(git rev-parse --show-toplevel); then
      exit_error "You are not in a git directory"
    fi

    if [ "$(pwd)" != "$root_dir" ]; then
      exit_error "Please run this script from the root directory: $root_dir"
    fi
}

delete_remote() {
    if [ "$(git remote | grep -c $REMOTE)" -gt 0 ]; then
      git remote remove $REMOTE
    fi
}

create_remote() {
    delete_remote
    if ! git remote add $REMOTE "$GIT_REPO_URL" -f; then
      exit_error "Unable add remote."
    fi
}

check_commits() {
    heroku_commit=$(heroku config:get HEROKU_SLUG_COMMIT --app "od-${APPS[0]}-$FROM_ENV")
    git_commit=$(git rev-parse "$REMOTE/$FROM_BRANCH")
    if [ "$heroku_commit" != "$git_commit" ]; then
        echo -n "Heroku commit: " && git --no-pager log "$heroku_commit" --pretty=format:'%Cred%h%Creset %s %Cgreen(%cr) %C(bold blue)<%an>%Creset%n' --no-walk
        echo -n "Git commit   : " && git --no-pager log "$git_commit" --pretty=format:'%Cred%h%Creset %s %Cgreen(%cr) %C(bold blue)<%an>%Creset%n' --no-walk
        exit_error "Looks like what is currently deployed on Heroku does not match git branch"
    fi
}

check_env_vars_diff() {
    log_info "Checking diff in environment variables"
    GIT_DIFF_CMD="git diff $to_commit..$from_commit -- $ENV_FILES"
    DIFF=$(eval "$GIT_DIFF_CMD")
    if [ -n "$DIFF" ]; then
        execute "$GIT_DIFF_CMD"
        log_warning "Some diff have been found, make sure to update the environment variables 🧐"
    else
        log_success "No diff found, you are good to go 🥳"
    fi
}

git_push() {
    LOCAL_BRANCH=promote

    log_info "Pushing diff on git"

    if [ "$(git branch | grep -c $REMOTE)" -gt 0 ]; then
      git branch -D $LOCAL_BRANCH
    fi

    if ! git checkout -b $LOCAL_BRANCH "$REMOTE/$FROM_BRANCH"; then
      exit_error "Unable to checkout $FROM_BRANCH to $LOCAL_BRANCH."
    fi

    if ! git push $REMOTE "$LOCAL_BRANCH:$TO_BRANCH"; then
      exit_error "Unable to push $FROM_BRANCH to $TO_BRANCH. Please rebase then try again."
    fi

    git checkout -
    git branch -D $LOCAL_BRANCH
}

backup_database() {
    log_info "Creating DB backup..."
    execute heroku pg:backups:capture -a "od-${APPS[0]}-$TO_ENV"
}

promote_heroku() {
    for app in "${APPS[@]}"
    do
        execute heroku pipelines:promote --app "od-$app-$FROM_ENV" --to "od-$app-$TO_ENV"
    done

    log_info "Waiting for dynos to be up..."
    for url in "${HEALTH_CHECK_URLS[@]}"
    do
      while [[ "$(curl -s -o /dev/null -L -w ''%{http_code}'' "$url")" != "200" ]]
      do
        echo "curl $url"
        sleep 2
      done
    done
}

deploy() {
    log_info "Retrieving apps infos..."
    from_commit=$REMOTE/$FROM_BRANCH
    to_commit=$REMOTE/$TO_BRANCH

    print "Checking diff from $to_commit to $from_commit"

    log_info "Checking diff to be loaded in $TO_ENV"
    git log --color --graph --pretty=format:'%Cred%h%Creset %s %Cgreen(%cr) %C(bold blue)<%an>%Creset' $to_commit..$from_commit | tee

    echo
    if ask "OK to continue"; then
      check_env_vars_diff
    else
      exit_error "Aborting"
    fi

    echo
    if ask "OK to continue"; then
        if [ $SKIP_DB_DUMP -eq 0 ]
        then
            backup_database
        fi
        git_push
        promote_heroku
    fi
}

usage() {
  echo "Usage: $0 [ --staging | --production ] [ --force ] [ --skip-db-dump ]"
  echo "  --staging         Promote to staging"
  echo "  --production      Promote to production"
  echo "  --force           Force deploy even if commits are not the same"
  echo "  --skip-db-dump    Skip database dump"
  echo ""
}

while [[ $# -gt 0 ]]; do
  case $1 in
    --staging)
      FROM_ENV=develop
      FROM_BRANCH=main
      TO_ENV=staging
      TO_BRANCH=staging
      HEALTH_CHECK_URLS=(https://staging-api.onlydust.com/actuator/health https://staging-bo-api.onlydust.com/actuator/health)
      shift
      ;;
    --production)
      FROM_ENV=staging
      FROM_BRANCH=staging
      TO_ENV=production
      TO_BRANCH=production
      HEALTH_CHECK_URLS=(https://api.onlydust.com/actuator/health https://bo-api.onlydust.com/actuator/health)
      shift
      ;;
    --force)
      FORCE=1
      shift
      ;;
    --skip-db-dump)
      SKIP_DB_DUMP=1
      shift
      ;;
    --help | -h)
      usage
      exit 0
      ;;
    *)
      exit_error "Error: unrecognized option '$1'"
      ;;
  esac
done

check_args
check_command git
check_command heroku
check_cwd
create_remote

if [ $FORCE -eq 0 ]
then
    check_commits
fi

deploy

delete_remote

exit_success
