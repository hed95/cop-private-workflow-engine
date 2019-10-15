# Workflow Engine

Central workflow engine


Functional packaging constructs

#### REST API Controllers

Interface that is exposed over HTTP and define the resources for the engine

#### Application Service Code

Internal application code that uses the native Camunda API and does filtering
based on the user who is currently logged in and their team details.

Only certain Camunda REST APIs

#### Core Resources
1. Tasks
2. Process Definitions
3. Notifications
4. Shift
5. Process Instances


See swagger docs:

{env}://swagger-ui.html

If this service is talking to internal services then ensure you have added the internal CA certs as seperate certificate files 
to /ca/xxx.crt. These will be loaded as part of the boot procedure into the java keystore and trusted.

# Drone secrets

Name|Example value
---|---
dev_drone_aws_access_key_id|https://console.aws.amazon.com/iam/home?region=eu-west-2#/users/bf-it-devtest-drone?section=security_credentials
dev_drone_aws_secret_access_key|https://console.aws.amazon.com/iam/home?region=eu-west-2#/users/bf-it-devtest-drone?section=security_credentials
drone_public_token|Drone token (Global for all github repositories and environments)
engine_port|8080
engine_private_der_path|/tmp/private_key.der
engine_public_der_path|/tmp/public_key.der
env_api_cop_url|operational-data-api.dev.cop.homeoffice.gov.uk, operational-data-api.staging.cop.homeoffice.gov.uk, operational-data-api.cop.homeoffice.gov.uk
env_api_ref_url|api.dev.refdata.homeoffice.gov.uk, api.staging.refdata.homeoffice.gov.uk, api.refdata.homeoffice.gov.uk
env_db_engine_default_dbname|xxx
env_db_engine_default_password|xxx
env_db_engine_default_username|xxx
env_db_engine_driver|org.postgresql.Driver
env_db_engine_hostname|privatecopdevws.cptlzykvnlia.eu-west-2.rds.amazonaws.com, privatecopstagingws.crckizhiyjmt.eu-west-2.rds.amazonaws.com, privatecopws.crckizhiyjmt.eu-west-2.rds.amazonaws.com
env_db_engine_jdbc_options|?sslmode=require&currentSchema=public
env_db_engine_options|?ssl=true
env_db_engine_port|5432
env_db_engine_type|postgres
env_engine_cors|https://www.dev.cop.homeoffice.gov.uk,https://translation.dev.cop.homeoffice.gov.uk,https://api-form.dev.cop.homeoffice.gov.uk, https://www.staging.cop.homeoffice.gov.uk,https://translation.staging.cop.homeoffice.gov.uk,https://api-form.staging.cop.homeoffice.gov.uk, https://www.cop.homeoffice.gov.uk,https://translation.cop.homeoffice.gov.uk,https://api-form.cop.homeoffice.gov.uk
env_engine_image|quay.io/ukhomeofficedigital/cop-private-workflow-engine
env_engine_java_opts|
env_engine_keycloak_client_id|keycloak client name
env_engine_keycloak_client_secret|xxx
env_engine_name|engine
env_engine_private_der|xxx
env_engine_public_der|xxx
env_engine_spring_profiles_active|swagger
env_engine_url|engine.dev.cop.homeoffice.gov.uk, engine.staging.cop.homeoffice.gov.uk, engine.cop.homeoffice.gov.uk
env_gov_notify_api_key|xxx
env_gov_notify_notification_email_template_id|x
env_gov_notify_notification_sms_template_id|x
env_keycloak_realm|cop-dev, cop-staging, cop-prod
env_keycloak_url|sso-dev.notprod.homeoffice.gov.uk/auth, sso.digital.homeoffice.gov.uk/auth
env_kube_namespace_private_cop|private-cop-dev, private-cop-staging, private-cop
env_kube_server|https://kube-api-notprod.notprod.acp.homeoffice.gov.uk, https://kube-api-prod.prod.acp.homeoffice.gov.uk
env_kube_token|xxx
env_redis_ssl|true
env_redis_token|xxx
env_redis_url|cop-dev-redis-rg-001.cop-dev-redis-rg.obrtxl.euw2.cache.amazonaws.com, cop-staging-redis-rg-001.cop-staging-redis-rg.swzhug.euw2.cache.amazonaws.com, cop-prod-redis-rg-001.cop-prod-redis-rg.swzhug.euw2.cache.amazonaws.com
env_whitelist|comma separated x.x.x.x/x list
env_www_url|www.dev.cop.homeoffice.gov.uk, www.staging.cop.homeoffice.gov.uk, www.cop.homeoffice.gov.uk
nginx_image|quay.io/ukhomeofficedigital/nginx-proxy
nginx_tag|latest
production_drone_aws_access_key_id|https://console.aws.amazon.com/iam/home?region=eu-west-2#/users/bf-it-prod-drone?section=security_credentials
production_drone_aws_secret_access_key|https://console.aws.amazon.com/iam/home?region=eu-west-2#/users/bf-it-prod-drone?section=security_credentials
protocol_awbs|awbs://
protocol_https|https://
protocol_jdbc|jdbc:postgresql://
protocol_postgres|postgres://
quay_password|xxx (Global for all repositories and environments)
quay_username|docker (Global for all repositories and environments)
slack_webhook|https://hooks.slack.com/services/xxx/yyy/zzz (Global for all repositories and environments)
staging_drone_aws_access_key_id|https://console.aws.amazon.com/iam/home?region=eu-west-2#/users/bf-it-prod-drone?section=security_credentials
staging_drone_aws_secret_access_key|https://console.aws.amazon.com/iam/home?region=eu-west-2#/users/bf-it-prod-drone?section=security_credentials
