# Documentation

A Lambda written in Clojure, compiled to a native image, deployed to Localstack and AWS via Terraform using DynamoDB to tell you how many days to your birthday.

# How to run locally

Install clojure
Install babashka
Install terraform
Install docker

Compile like so:

```
bb hl:native:executable
```

This will run a Holy Lambda task that will generate a native image.

Deploy to Localstack.

```
docker-compose up
terraform -chdir=terraform/environments/local init
terraform -chdir=terraform/environments/local plan
terraform -chdir=terraform/environments/local apply
```

Deploy to AWS by setting your VPC ID and region in terraform/environments/aws/terraform.tfvars.json (or on the command line) and running these:

```
terraform -chdir=terraform/environments/aws init
terraform -chdir=terraform/environments/aws plan
terraform -chdir=terraform/environments/aws apply
```

The terraform output is an API Gateway URL to call, e.g.

http://ftyes33eaw.execute-api.localhost.localstack.cloud:4566/local/

Add `hello/username` to this URL to get the defined API entrypoints.

GETs to this route will read from a DynamoDB instance and tell you how many days until the user's birthday (if known)

PUTs to this route with an `application/json` content-type and a body of the form `{"dateOfBirth":"YYYY-mm-dd"}` will store and return a 204

The holy-lambda documentation is available [here](https://fierycod.github.io/holy-lambda).

# Developing

To enter the REPL with a DB target of `mock` (which is only useful in the REPL) :

```
DB_TYPE=mock clj
```

Or to target Localstack's DynamoDB :

```
AWS_ACCESS_KEY_ID=test AWS_SECRET_ACCESS_KEY=test \
DB_TYPE=dynamodb DB_ENDPOINT_PORT=4566 \
DB_ENDPOINT_PROTOCOL=http LOCALSTACK_HOST=localhost \
DB_ENDPOINT_HOST='$LOCALSTACK_HOST' AWS_REGION=us-east-1 clj
```

Then start the server:

```
(use 'bmcgavin.birthday.api :reload-all) (stop-server) (bmcgavin.birthday.api/app)
```

You can now use http://localhost:8080/ as your endpoint. Restart the server after changes. If you add dependencies (via deps.edn) then you need to ctrl-c out of the REPL, and clean/compile:

```
bb hl:clean
bb hl:compile
```

# Testing

To run the test suite :

```
clj -X:test
```

The Dynamo tests require a running Localstack container.

# Architecture

Any changes to the API Gateway resources will cause a new API Gateway deployment to trigger, the create_before_destroy lifecycle means that this will cause no downtime.

Deployments can be pushed to staging first as all resources are scoped to the environment. This combination gives a possibility of no downtime deployments.

This concept of overlapping environments can co-exist in the same VPC, as shown:

![Architecture diagram of the VPC containing a pair of deployments, each with an API Gateway with two endpoints that each trigger the same Lambda, which accesses DynamoDB](images/Technical%20Diagrams%20-%20VPC.jpg)
