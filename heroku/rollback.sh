#!/bin/bash

SCRIPT_DIR=`readlink -f $0 | xargs dirname`
. $SCRIPT_DIR/utils.sh

ENVIRONMENT=

check_args() {
    if [[ -z $ENVIRONMENT ]]; then
      exit_error "Invalid arguments, you must specify at least --staging or --production flag"
    fi
}

manage_apps() {
    action=$1
    for app in ${ALL_BACKENDS[@]}; do
        heroku ps --app $app --json | jq '.[] | .type' | while read dyno; do
            execute heroku ps:$action $dyno --app $app
        done
    done
}

stop_apps() {
    log_info "Stopping the apps"
    manage_apps stop
}

start_apps() {
    log_info "Starting back the apps"
    manage_apps restart
}

rollback_backends() {
    for app in ${ALL_BACKENDS[@]}; do
        if ask "Do you want to rollback $app ?"; then
            execute heroku releases --app $app
            read -p "Which release do you want to rollback to ? (leave blank for 1 release) " rollback_release
            execute heroku rollback $rollback_release --app $app
        fi
    done
}

rollback_database() {
    current_database=`heroku addons:info DATABASE -a $DB_BILLING_APP | sed -n 's/=== \(.*\)/\1/p'`
    [ -z $current_database ] && exit_error "Unable to get the current database"
    log_info "Current database is '$current_database':"
    heroku addons:info DATABASE -a $DB_BILLING_APP

    read -p "How much time in the past do you want to rollback to ? (e.g. 1 hour) " rollback_time
    execute heroku addons:create heroku-postgresql:standard-0 --rollback DATABASE --by \'$rollback_time\' --app $DB_BILLING_APP
    rollback_database=`sed -n 's/\(.*\) is being created in the background. .*/\1/p' $LOG_FILE`
    [ -z $rollback_database ] && exit_error "Unable to create the rollback database"
    log_info "Rollback database: $rollback_database"

    execute heroku pg:wait --app $DB_BILLING_APP

    ask "OK to promote $rollback_database for all apps"
    if [ $? -eq 0 ]; then
        for app in ${ALL_DB_CONNECTED_APPS[@]}; do
            execute heroku addons:attach $rollback_database -a $app
            execute heroku pg:promote $rollback_database -a $app
            execute heroku addons:detach $current_database -a $app
        done
    fi

    ask "Do you want to destroy old database ($current_database) ?"
    if [ $? -eq 0 ]; then
        execute heroku addons:destroy $current_database
    fi
}


while [[ $# -gt 0 ]]; do
  case $1 in
    --staging)
      ENVIRONMENT=staging
      shift
      ;;
    --production)
      ENVIRONMENT=production
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
check_command heroku
check_command jq

DB_BILLING_APP=od-hasura-$ENVIRONMENT
ALL_DB_CONNECTED_APPS=(
    od-hasura-$ENVIRONMENT
    od-hasura-auth-$ENVIRONMENT
    od-api-$ENVIRONMENT
    od-github-indexer-$ENVIRONMENT
    od-indexer-$ENVIRONMENT
    od-new-api-$ENVIRONMENT
    od-back-office-api-$ENVIRONMENT
)

ALL_BACKENDS=(
    od-new-api-$ENVIRONMENT
    od-back-office-api-$ENVIRONMENT
    od-indexer-$ENVIRONMENT
    od-api-$ENVIRONMENT
    od-github-indexer-$ENVIRONMENT
)


stop_apps

ask "Do you want to rollback the backends"
if [ $? -eq 0 ]; then
    rollback_backends
fi

ask "Do you want to rollback the database"
if [ $? -eq 0 ]; then
    rollback_database
fi

start_apps

log_info "📌 Do not forget to rollback Retool apps 😉"

exit_success
