#!/bin/bash

SCRIPT_DIR=$(readlink -f "$0" | xargs dirname)
. "$SCRIPT_DIR"/utils.sh

unset -v FROM_ENV
unset -v FROM_BRANCH
unset -v TO_BRANCH
unset -v TO_ENV
unset -v MAINTENANCE
unset -v MAINTENANCE_REPO_DIR


while [[ $# -gt 0 ]]; do
  case $1 in
    --from|-f)
      FROM_ENV=$2
      FROM_BRANCH=$([ "$FROM_ENV" = "develop" ] && echo main || echo "$FROM_ENV")
      shift 2
      ;;
    --to|-t)
      TO_ENV=$2
      TO_BRANCH=$([ "$TO_ENV" = "develop" ] && echo main || echo "$TO_ENV")
      shift 2
      ;;
    --help | -h)
      usage
      exit 0
      ;;
    *)
      exit_error "❌ Error: unrecognized option '$1'"
      ;;
  esac
done

REMOTE=promote-origin
FROM_COMMIT=$REMOTE/$FROM_BRANCH
TO_COMMIT=$REMOTE/$TO_BRANCH

ENV_FILES="**/application.yaml"

check_args() {
    if [[ -z $FROM_ENV || -z $TO_ENV ]]; then
      exit_error "❌ Invalid arguments, you must specify --from and --to options"
    fi
}

check_cwd() {
    if ! root_dir=$(git rev-parse --show-toplevel); then
      exit_error "❌ You are not in a git directory"
    fi

    if [ "$(pwd)" != "$root_dir" ]; then
      exit_error "❌ Please run this script from the root directory: $root_dir"
    fi
}

delete_remote() {
    if [ "$(git remote | grep -c $REMOTE)" -gt 0 ]; then
      git remote remove $REMOTE
    fi
}

create_remote() {
    delete_remote
    GIT_REPO_URL=$(git remote | head -1 | xargs git remote get-url)
    if ! git remote add $REMOTE "$GIT_REPO_URL" -f; then
      exit_error "❌ Unable add remote."
    fi
}

check_env_vars_diff() {
    log_info "🧐 Checking diff in environment variables"
    GIT_DIFF_CMD="git diff $TO_COMMIT..$FROM_COMMIT -- $ENV_FILES"
    DIFF=$(eval "$GIT_DIFF_CMD")

    if [ -z "$DIFF" ]; then
        log_success "✅ No diff found, you are good to go 🥳"
        return
    fi

    execute "$GIT_DIFF_CMD"
    log_warning "⚠️ Some diff have been found, make sure to update the environment variables"
}

git_push() {
    LOCAL_BRANCH=promote

    log_info "🚀 Pushing diff on git"

    if [ "$(git branch | grep -c $REMOTE)" -gt 0 ]; then
      git branch -D "$LOCAL_BRANCH"
    fi

    if ! git checkout -b "$LOCAL_BRANCH" "$REMOTE/$FROM_BRANCH"; then
      exit_error "❌ Unable to checkout $FROM_BRANCH to $LOCAL_BRANCH."
    fi

    if ! git push "$REMOTE" "$LOCAL_BRANCH:$TO_BRANCH"; then
      log_error "❌ Unable to push $LOCAL_BRANCH to $TO_BRANCH."

      if ask "Do you want to force push"; then
        if ! execute git push -f "$REMOTE" "$LOCAL_BRANCH:$TO_BRANCH"; then
          exit_error "❌ Unable to force push $FROM_BRANCH to $TO_BRANCH."
        fi
      fi
    fi

    git checkout -
    git branch -D $LOCAL_BRANCH
}

deploy() {
    log_info "🧐 Checking diff to be loaded in $TO_BRANCH ($TO_COMMIT..$FROM_COMMIT)"
    git log --color --graph --pretty=format:'%Cred%h%Creset %s %Cgreen(%cr) %C(bold blue)<%an>%Creset' $TO_COMMIT..$FROM_COMMIT | tee

    echo
    if ask "OK to continue"; then
      check_env_vars_diff

      echo
      if ask "OK to continue"; then
          git_push
      fi
    fi
}

activate_maintenance() {
  ask "Do you want to activate maintenance mode during deployment"
  MAINTENANCE=$((1 - $?))

  if [ $MAINTENANCE -eq 0 ]; then
    log_warning "⚠️  Skipping maintenance activation"
    return
  fi

  log_info "🛠️ Activating maintenance mode"
  MAINTENANCE_REPO_DIR=$(mktemp -d)

  (
    cd "$MAINTENANCE_REPO_DIR" || exit_error "❌ Unable to change directory"
    execute git clone https://github.com/onlydustxyz/od-maintenance-page.git .

    if [ "$(npx wrangler whoami | grep -c 'You are not authenticated')" -eq 1 ]; then
      execute npx wrangler login
    fi
    execute npx wrangler deploy --env "$TO_ENV"
  )
}

deactivate_maintenance() {
  if [ $MAINTENANCE -eq 0 ]; then
    return
  fi

  if ! ask "Do you want to deactivate maintenance mode"; then
    log_warning "⚠️ Skipping maintenance deactivation, to deactivate maintenance, use the following command:"
    log_warning "(cd $MAINTENANCE_REPO_DIR && npx wrangler delete --env $TO_ENV)"
    return
  fi

  log_info "🛠️ Deactivating maintenance"
  (
      cd "$MAINTENANCE_REPO_DIR" || exit_error "❌ Unable to change directory"
      execute npx wrangler delete --env "$TO_ENV"
  )

  [ -d "$MAINTENANCE_REPO_DIR" ] && rm -rf "$MAINTENANCE_REPO_DIR"
}

usage() {
  echo "Usage: $0 [ --from ENV ] [ --to ENV ]"
  echo "  --from | -f ENV     The source environment to promote from (develop,staging,perf,production)"
  echo "  --to | -t ENV       The target environment to promote to (develop,staging,perf,production)"
  echo ""
}

check_args
check_commands git aws npm gh
check_cwd

activate_maintenance

create_remote

deploy

log_info "🔄 Waiting for deployment to complete"
"$SCRIPT_DIR"/wait_for_deployment.sh -e "$TO_ENV" -c "$REMOTE/$TO_BRANCH"

deactivate_maintenance

delete_remote

exit_success
