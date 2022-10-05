#pragma once

#include <string>
#include <vector>
#include <sstream>
#include <iostream>
#include <map>

struct HttpRequest {
    std::string method;
    std::string path;
    std::map<std::string, std::string> params;
    std::map<std::string, std::string> headers;
    std::string data;
};

struct HttpResponse {
    int statusCode;
    std::string status;
    std::map<std::string, std::string> headers;
    std::string data;
};

std::string CreateHttpResponse(HttpResponse resp);
std::vector<std::string> Split(std::string s, std::string sep);
HttpRequest ParseRequest(std::string data);