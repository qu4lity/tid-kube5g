#!/bin/sh

QUEUE_NAME=$1
MESSAGE=$2

get_queue_url()
{
    test -n $QUEUE_NAME

    ## Get Queue URL from AWS (as JSON reposonse)
    __tmp=$(aws \
                --output=json \
                sqs get-queue-url --queue-name $QUEUE_NAME) \
        || exit "failed to get queue URL for '$QUEUE_NAME'"


    ## return queue URL as a string)
    echo "$__tmp" | jq -r '.QueueUrl' \
        || exit "failed to extract queue URL from JSON for '$1'"
}


QUEUE_URL=$(get_queue_url "QUEUE_NAME") || exit 1

aws sqs send-message --queue-url $QUEUE_URL --message-body $MESSAGE
