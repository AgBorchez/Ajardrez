#pragma once

#include <cstdint>
#include <string>

enum class Color : int8_t { 
    NONE = 0, 
    WHITE = 1, 
    BLACK = -1 
};

enum class PieceType : int8_t { 
    NONE = 0, 
    PAWN, 
    KNIGHT, 
    BISHOP, 
    ROOK, 
    QUEEN, 
    KING 
};

struct Piece {
    PieceType type = PieceType::NONE;
    Color color = Color::NONE;
};

enum class MoveType : uint8_t {
    NORMAL = 0,
    PROMOTION,
    CASTLE_KINGSIDE,
    CASTLE_QUEENSIDE
};

struct Move {
    int from;
    int to;
    Piece captured;
    MoveType type;
    PieceType promotion_piece;

    Move(int f = 0, int t = 0, Piece cap = {}, MoveType mt = MoveType::NORMAL, PieceType promo = PieceType::NONE) 
        : from(f), to(t), captured(cap), type(mt), promotion_piece(promo) {}
};

namespace SquareUtils {
    inline bool is_valid(int sq) { return sq >= 0 && sq < 64; }
    inline int row(int sq) { return sq / 8; }
    inline int col(int sq) { return sq % 8; }

    inline int from_string(const std::string& s) {
        if (s.length() < 2) return -1;
        int c = s[0] - 'a';
        int r = s[1] - '1';
        return r * 8 + c;
    }

    inline std::string to_string(int sq) {
        char c = 'a' + (sq % 8);
        char r = '1' + (sq / 8);
        return {c, r};
    }
}