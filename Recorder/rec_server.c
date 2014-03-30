/*
 * File:   rec_server.c
 * Author: deepak
 *
 * Recorder
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <pthread.h>
#include <stdbool.h>
#include <errno.h>
#include <string.h>
#include <netinet/in.h>

#define MAX_CHARS       (32)
#define MAX_CLIENTS		(5)

#define LOGD    printf

typedef struct {
    pthread_t       thread;
    int             socket;
    bool            run;
	int				no_clients;
	pthread_t		rec_threads[MAX_CLIENTS];
	void			*clients[MAX_CLIENTS];
}recorder_t;

static void * rec_thread (void *param);

static int32_t create_listen_socket (recorder_t *inst, int port)
{
    struct sockaddr_in serv_addr;
    int ret = 0, reuse = 1;

    inst->socket = socket(AF_INET, SOCK_STREAM, 0);
    if (inst->socket < 0) {
        ret = -1;
    }

    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = INADDR_ANY;
    serv_addr.sin_port = htons(port);

	/* Set reuse option */
	setsockopt(inst->socket, SOL_SOCKET, SO_REUSEADDR, &reuse, sizeof(reuse));

    ret = bind(inst->socket, (struct sockaddr *) &serv_addr, sizeof(serv_addr));
    if (ret < 0)
        LOGD("ERROR on binding");

    ret = listen(inst->socket,5);
    if (ret < 0)
        LOGD("ERROR on listening");

    return ret;

}

static void cleanup_sockets (recorder_t *inst)
{
    if (-1 != inst->socket) {
        close (inst->socket);
    }
}
static void start_rec_client (recorder_t *inst, int cl_soc)
{
	pthread_attr_t  attr;
	recorder_t		*client;
	int				ret;

	client = (recorder_t *) malloc (sizeof (recorder_t));
    if (!client) {
        LOGD("RecService: Failed to create rec instance. no memory\n");
        return;
    }

	LOGD("RecClient: Starting thread\n");
    memset(client, 0, sizeof(*client));
    pthread_attr_init (&attr);
    client->run = true;
    client->socket = cl_soc;
    LOGD("RecClient: Client Soc = %d\n", client->socket);
    ret = pthread_create (&client->thread, &attr, rec_thread, client);
	if (0 == ret) {
		inst->no_clients++;
        pthread_attr_setdetachstate (&attr, PTHREAD_CREATE_DETACHED);
        pthread_detach(client->thread);
    }
	else {
		LOGD("RecClient: Start thread failed\n");
		free (client);
	}
    pthread_attr_destroy (&attr);
	return;
}

static void * rec_thread (void *param)
{
	recorder_t      *inst = (recorder_t *) param;
	int 		 	data_soc = inst->socket;
	unsigned char	buffer	[512];
	int				sbyts, fbyts;
	FILE			*fp = NULL;

	LOGD("RECClient: Start of REC thread: Run = %d\n", inst->run);
    LOGD("RecClient: soc = %d", data_soc);
	fp = fopen("client1.bin", "wb");

	/* TODO: List
	* - Check connection reset by peer
	* - Write to different files based on hrs / size
	* - use select for blocking until data is there
	*/
	while (inst->run) {
        LOGD("RecClient: Start read\n");
		sbyts = read (data_soc, buffer, sizeof(buffer));
        LOGD("RecClient: bytes received = %d\n", sbyts);
		if (sbyts > 0) {
			fbyts = fwrite(buffer, sizeof(unsigned char), sbyts, fp);
			fflush(fp);
		}
		else {
			LOGD("No Data on Socket.. Try again\n");
		}
		usleep(500);
	}

	LOGD("RecClient: Run is set to false\n");
	fclose (fp);
	cleanup_sockets(inst);
	free (inst);
	pthread_exit(0);
	return NULL;
}

static void * master_thread (void *param)
{
    recorder_t      *inst = (recorder_t *) param;
    int32_t         run, req_soc, ret;
	int32_t			req_len;
    struct sockaddr req_addr;
	recorder_t		*clnt;

	ret = create_listen_socket(inst, 6969);
	if (ret != 0) {
		LOGD("RecService: Failed to initialise sockets\n");
		inst->run = false;
	}

    while (inst->run) {
        LOGD("RecService: Waiting for connection\n");
        memset (&req_addr, 0, sizeof(req_addr));
        req_len = sizeof (req_addr);
        req_soc = accept(inst->socket, (struct sockaddr *) &req_addr, &req_len);
        if (req_soc >= 0) {
			/* Create a client thread for recording */
			LOGD("RecService: Received connection new soc = %d\n", req_soc);
			start_rec_client (inst, req_soc);
        }
    }

	for (run = 0; run < MAX_CLIENTS; run++) {
		clnt = (recorder_t *) inst->clients[run];
		if (!clnt) {
			clnt->run = false;
		}
	}

    LOGD("RecService: Cleaning socket\n");
    cleanup_sockets (inst);
    LOGD("RecService: After clean socket\n");
    free (inst);
    LOGD("RecService: Exiting thread\n");
    fflush (stdout);

    pthread_exit(0);
    // This is not required. Just to keep the compiler happy
    return NULL;
}


int32_t init_recorder (recorder_t **handle)
{
    recorder_t      *inst = NULL;
    int32_t         ret = 0;
    pthread_attr_t  attr;

    inst = (recorder_t *) malloc (sizeof (recorder_t));
    if (!inst) {
        LOGD("RecService: Failed to create instance. no memory\n");
        ret = -1;
        goto exit;
    }

    memset(inst, 0, sizeof(*inst));
    pthread_attr_init (&attr);
    ret = pthread_create (&inst->thread, &attr, master_thread, inst);
    if (0 == ret) {
        inst->run = true;
        pthread_attr_setdetachstate (&attr, PTHREAD_CREATE_DETACHED);
        pthread_detach(inst->thread);
    }
    pthread_attr_destroy (&attr);

exit:
    if (ret != 0) {
        free (inst);
        inst = NULL;
    }
    *handle = (void *)inst;
    return ret;
}

int32_t exit_identifier (recorder_t **handle)
{
    recorder_t      *inst = (recorder_t *) *handle;

    if (!handle) {
        LOGD("RecService: Invalid handle\n");
        return -1;
    }

    inst->run = false;
    *handle = NULL;
    return 0;
}

int main (void)
{
	recorder_t		*handler;
	init_recorder (&handler);
	
	while (1) {
		sleep(3);
	}
	
	return 0;
}
