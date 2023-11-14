# This file contains a list of environment variables that need to be set when running ZAC locally.

# To use this file you need to use the 1Password CLI extensions.
# Please see the docs/INSTALL.md file for instructions.
# Please see .env.example for descriptions of the environment variables.

# -------------------------
# ZAC environment variables
# -------------------------

AUTH_REALM=op://Dimpact/ZAC-.env-$APP_ENV/AUTH/REALM
AUTH_RESOURCE=op://Dimpact/ZAC-.env-$APP_ENV/AUTH/RESOURCE
AUTH_SECRET=op://Dimpact/ZAC-.env-$APP_ENV/AUTH/SECRET
AUTH_SERVER=op://Dimpact/ZAC-.env-$APP_ENV/AUTH/SERVER
BAG_API_CLIENT_MP_REST_URL=op://Dimpact/ZAC-.env-$APP_ENV/BAG/API_CLIENT_MP_REST_URL
BAG_API_KEY=op://Dimpact/ZAC-.env-$APP_ENV/BAG/API_KEY
BRP_API_CLIENT_MP_REST_URL=op://Dimpact/ZAC-.env-$APP_ENV/BRP/API_CLIENT_MP_REST_URL
BRP_API_KEY=op://Dimpact/ZAC-.env-$APP_ENV/BRP/API_KEY
CONTACTMOMENTEN_API_CLIENT_MP_REST_URL=op://Dimpact/ZAC-.env-$APP_ENV/CONTACTMOMENTEN/API_CLIENT_MP_REST_URL
CONTACTMOMENTEN_API_CLIENTID=op://Dimpact/ZAC-.env-$APP_ENV/CONTACTMOMENTEN/API_CLIENTID
CONTACTMOMENTEN_API_SECRET=op://Dimpact/ZAC-.env-$APP_ENV/CONTACTMOMENTEN/API_SECRET
CONTEXT_URL=op://Dimpact/ZAC-.env-$APP_ENV/CONTEXT_URL
DB_HOST=op://Dimpact/ZAC-.env-$APP_ENV/DB/HOST
DB_NAME=op://Dimpact/ZAC-.env-$APP_ENV/DB/NAME
DB_PASSWORD=op://Dimpact/ZAC-.env-$APP_ENV/DB/PASSWORD
DB_USER=op://Dimpact/ZAC-.env-$APP_ENV/DB/USER
GEMEENTE_CODE=op://Dimpact/ZAC-.env-$APP_ENV/GEMEENTE/CODE
GEMEENTE_MAIL=op://Dimpact/ZAC-.env-$APP_ENV/GEMEENTE/MAIL
GEMEENTE_NAAM=op://Dimpact/ZAC-.env-$APP_ENV/GEMEENTE/NAAM
KLANTEN_API_CLIENT_MP_REST_URL=op://Dimpact/ZAC-.env-$APP_ENV/KLANTEN/API_CLIENT_MP_REST_URL
KLANTEN_API_CLIENTID=op://Dimpact/ZAC-.env-$APP_ENV/KLANTEN/API_CLIENTID
KLANTEN_API_SECRET=op://Dimpact/ZAC-.env-$APP_ENV/KLANTEN/API_SECRET
KVK_API_CLIENT_MP_REST_URL=op://Dimpact/ZAC-.env-$APP_ENV/KVK/API_CLIENT_MP_REST_URL
KVK_API_KEY=op://Dimpact/ZAC-.env-$APP_ENV/KVK/API_KEY
LDAP_DN=op://Dimpact/ZAC-.env-$APP_ENV/LDAP/DN
LDAP_PASSWORD=op://Dimpact/ZAC-.env-$APP_ENV/LDAP/PASSWORD
LDAP_URL=op://Dimpact/ZAC-.env-$APP_ENV/LDAP/URL
LDAP_USER=op://Dimpact/ZAC-.env-$APP_ENV/LDAP/USER
MAILJET_API_KEY=op://Dimpact/ZAC-.env-$APP_ENV/MAILJET/API_KEY
MAILJET_API_SECRET_KEY=op://Dimpact/ZAC-.env-$APP_ENV/MAILJET/API_SECRET_KEY
MAX_FILE_SIZE_MB=op://Dimpact/ZAC-.env-$APP_ENV/MAX_FILE_SIZE_MB
OPA_API_CLIENT_MP_REST_URL=op://Dimpact/ZAC-.env-$APP_ENV/OPA/API_CLIENT_MP_REST_URL
OPEN_NOTIFICATIONS_API_SECRET_KEY=op://Dimpact/ZAC-.env-$APP_ENV/OPEN_NOTIFICATIONS/API_SECRET_KEY
SD_AUTHENTICATION=op://Dimpact/ZAC-.env-$APP_ENV/SD/AUTHENTICATION
SD_CLIENT_MP_REST_URL=op://Dimpact/ZAC-.env-$APP_ENV/SD/CLIENT_MP_REST_URL
SD_FIXED_USER_NAME=op://Dimpact/ZAC-.env-$APP_ENV/SD/SD_FIXED_USER_NAME
SOLR_URL=op://Dimpact/ZAC-.env-$APP_ENV/SOLR_URL
VRL_API_CLIENT_MP_REST_URL=op://Dimpact/ZAC-.env-$APP_ENV/VRL/API_CLIENT_MP_REST_URL
ZGW_API_CLIENT_MP_REST_URL=op://Dimpact/ZAC-.env-$APP_ENV/ZGW/API_CLIENT_MP_REST_URL
ZGW_API_CLIENTID=op://Dimpact/ZAC-.env-$APP_ENV/ZGW/API_CLIENTID
ZGW_API_SECRET=op://Dimpact/ZAC-.env-$APP_ENV/ZGW/API_SECRET
ZGW_API_URL_EXTERN=op://Dimpact/ZAC-.env-$APP_ENV/ZGW/API_URL_EXTERN

# -----------------------------------------
# e2e only environment variables
# -----------------------------------------
E2E_TEST_USER_1_USERNAME=op://Dimpact/e2etesuser1/username
E2E_TEST_USER_1_PASSWORD=op://Dimpact/e2etesuser1/password
ZAC_URL=op://Dimpact/zaakafhandelcomponent-zac-dev/website


# -----------------------------------------
# Docker Compose only environment variables
# -----------------------------------------
DOCKER_COMPOSE_LDAP_TEST_USER_1_EMAIL_ADDRESS=op://Dimpact/ZAC-.env-$APP_ENV/DOCKER_COMPOSE/LDAP_TEST_USER_1_EMAIL_ADDRESS
DOCKER_COMPOSE_LDAP_TEST_USER_2_EMAIL_ADDRESS=op://Dimpact/ZAC-.env-$APP_ENV/DOCKER_COMPOSE/LDAP_TEST_USER_2_EMAIL_ADDRESS
DOCKER_COMPOSE_LDAP_GROUP_A_EMAIL_ADDRESS=op://Dimpact/ZAC-.env-$APP_ENV/DOCKER_COMPOSE/LDAP_GROUP_A_EMAIL_ADDRESS
