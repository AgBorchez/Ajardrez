#pragma once

#include "types.hpp"

namespace Ajardrez
{
        struct HistoryEntry
    {
        Move move;
        CastlingRights castling;
    };

    class Board {
    private:
        Piece squares[64];
        Color current_turn;
        CastlingRights castling;
        HistoryEntry history[512];
        
        int history_index = 0;

    public:
        Board();

        void reset();
        void setup_initial_position();

        Color get_turn() const { return current_turn; }
        void set_turn(Color turn) { current_turn = turn; }
        Piece get_piece(int sq) const { return squares[sq]; }
        void set_piece(int sq, Piece piece) { squares[sq] = piece; }

        const CastlingRights& get_castling() const { return castling; }

        void make_move(const Move& move);
        bool undo_move();

        int find_king_square(Color color) const;
    };

}
