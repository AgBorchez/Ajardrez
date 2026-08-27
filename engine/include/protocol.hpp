#pragma once

#include "board.hpp"
#include <string>

class Protocol {
private:
    Board board;

public:
    Protocol();

    std::string process_command(const std::string& line);

    void run();
};