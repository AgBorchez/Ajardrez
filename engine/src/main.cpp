#include "protocol.hpp"
#include <iostream>

int main() {
    std::ios_base::sync_with_stdio(false);
    std::cin.tie(NULL);

    Protocol protocol;
    protocol.run();

    return 0;
}