#include "http.h"

std::string CreateHttpResponse(HttpResponse resp) {
    std::stringstream ss;
    ss << "HTTP/1.1 " << resp.statusCode << " " << resp.status << "\n";
    for(auto header : resp.headers) ss << header.first << ": " << header.second << "\n";
    ss << "\n";
    ss << resp.data;

    return ss.str();
}

std::vector<std::string> Split(std::string s, std::string sep) {
    std::string curr;
    std::vector<std::string> ans;
    for(int i = 0; i + sep.size() <= s.size(); i++) {
        if(s.substr(i, sep.size()) == sep) {
            i += sep.size() - 1;
            ans.push_back(curr);
            curr.clear();
        } else {
            curr += s[i];
        }
    }
    if(sep.size() > 1) {
        curr += s.substr(s.size() - sep.size() + 1);
    }
    ans.push_back(curr);
    return ans;
}

HttpRequest ParseRequest(std::string data) {
    HttpRequest ans;
    std::vector<std::string> lines = Split(data, "\r\n");

    {
        std::vector<std::string> fline = Split(lines[0], " ");
        ans.method = fline[0];
        std::vector<std::string> pathAndParams = Split(fline[1], "?");
        ans.path = pathAndParams[0];
        if(pathAndParams.size() > 1) {
            std::vector<std::string> params = Split(pathAndParams[1], "&");
            for(auto p : params) {
                std::vector<std::string> param = Split(p, "=");
                ans.params[param[0]] = param[1];
            }
        }
    }

    {
        int headerCount = 0;
        while(lines[headerCount + 1].size() != 0) {
            std::vector<std::string> header = Split(lines[headerCount + 1], ": ");
            while(header[1][0] == ' ') header[1] = header[1].substr(1);
            ans.headers[header[0]] = header[1];
            headerCount++;
        }
    }

    int dataOffset = 0;
    for(std::string line : lines) {
        dataOffset += line.size() + 2;
        if(line.size() == 0) break;
    }
    ans.data = data.substr(dataOffset);

    return ans;
}