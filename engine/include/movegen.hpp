#pragma once

#include "board.hpp"
#include <vector>

class MoveGen {
public:
    static bool is_square_attacked(const Board& board, int sq, Color attacker_color);
    static bool is_in_check(const Board& board, Color color);

    static std::vector<Move> get_pseudo_legal_moves(const Board& board, int from_sq);
    static std::vector<Move> get_legal_moves(Board& board, int from_sq);
    static std::vector<Move> generate_all_legal_moves(Board& board, Color side);
};