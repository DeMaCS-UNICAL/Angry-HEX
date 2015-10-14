/*****************************************************************************
** ANGRYBIRDS AI AGENT FRAMEWORK
** Copyright (c) 2014, XiaoYu (Gary) Ge, Jochen Renz, Stephen Gould,
**   Sahan Abeyasinghe, Jim Keys,  Andrew Wang,
**   Peng Zhang
** All rights reserved.
**
** This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
** To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
** or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
*****************************************************************************/

#include <cstdlib>
#include <cassert>
#include <cstdio>
#include <iostream>
#include <fstream>
#include <cstring>
#include <string>
#include <sys/types.h>

#define _BSD_SOCKLEN_T_
#include <sys/socket.h>

#include <netinet/in.h>
#include <netdb.h>
#include <errno.h>
#include <unistd.h>
#include <arpa/inet.h>

using namespace std;

// Constants -----------------------------------------------------------------

#define DEFAULT_HOST "localhost"
#define DEFAULT_PORT 2004

#define CMD_SCREENSHOT 0x0B

// initClientComms -----------------------------------------------------------

int initClientComms(uint16_t port, const string& host)
{
    int client_socket = socket(PF_INET, SOCK_STREAM, 0);
    if (client_socket < 0) {
        cerr << "COMMS: error initializing client socket (socket)" << endl;
        exit(-1);
    }

    struct sockaddr_in server_name;
    struct hostent *hostinfo;
    server_name.sin_family = AF_INET;
    server_name.sin_port = htons(port);

    hostinfo = gethostbyname(host.c_str());
    if (hostinfo == NULL) {
        cerr << "COMMS: unknown host " << host.c_str() << endl;
        exit(-1);
    }
    server_name.sin_addr = *(struct in_addr *)hostinfo->h_addr;

    if (connect(client_socket, (struct sockaddr *)&server_name, sizeof(server_name)) < 0) {
        cerr << "COMMS: error connecting to server" << endl;
        exit(-1);
    }

    return client_socket;
}

// getScreenshot -------------------------------------------------------------

bool getScreenshot(int socket)
{
    // send screenshot message
    unsigned char cmd = CMD_SCREENSHOT;
    if (write(socket, &cmd, 1) < 0) {
        cerr << "COMMS: error sending connect message" << endl;
        return false;
    }

    // wait for response
    struct timeval timeout;
    timeout.tv_sec = (int)5;
    timeout.tv_usec = 0;

    if (setsockopt(socket, SOL_SOCKET, SO_RCVTIMEO, (char *)&timeout, sizeof(struct timeval)) < 0) {
        cerr << "COMMS: error waiting for server to respond" << endl;
        exit(-1);
    }

    // allocate space for the message
    const int nBytes = 1024;
    unsigned char *buffer = new unsigned char[nBytes];
    string message;

    // receive the message
    while (true) {
        int bytesRead = read(socket, buffer, nBytes);

        if (bytesRead < 0) {
            cerr << "COMMS: error receiving message after " << message.size() << " bytes" << endl;
            delete[] buffer;
            return false;
        }

        message.insert(message.size(), (const char *)&buffer[0], bytesRead);
        if (bytesRead < nBytes)
            break;
    }

    delete[] buffer;
    
    cerr << "...read " << message.size() << endl;
    assert(message.size() > 8);

    // decode the message
    uint32_t n;
    memcpy(&n, &message[0], sizeof(uint32_t));
    const int width = (int)ntohl(n);
    memcpy(&n, &message[4], sizeof(uint32_t));
    const int height = (int)ntohl(n);
    
    cerr << "...image is " << width << "-by-" << height << endl;


    return true;
}

// Main ----------------------------------------------------------------------

int main(int argc, char *argv[])
{
    string host = DEFAULT_HOST;
    uint16_t port = DEFAULT_PORT;

    // process command line arguments
    if (argc > 2) {
        cerr << "USAGE: " << argv[0] << " [[host:]port]" << endl;
        return 0;
    }

    if (argc == 2) {
        string hostString(argv[1]);
        size_t indexOfColon = hostString.find(':', 0);
        if(indexOfColon == string::npos) {
            port = atoi(argv[2]);
        } else {
            host = hostString.substr(0, indexOfColon);
            port = atoi(hostString.substr(indexOfColon + 1, hostString.size()).c_str());
        }
    }

    // initialize client communications
    cerr << "CLIENT: connecting to server " << host << ":" << port << endl;
    const int client_socket = initClientComms(port, host);

    // request a screenshot
    cerr << "CLIENT: requesting a screenshot" << endl;
    getScreenshot(client_socket);

    return 0;
}
