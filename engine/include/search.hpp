#pragma once

#include "board.hpp"

namespace Ajardrez {
    class Search {
    public:
        static Move find_best_move(Board& board, int depth);

    private:
        static int minimax(Board& board, int depth, int alpha, int beta, bool is_maximizing);
    };

}