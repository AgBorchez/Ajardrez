#include "movegen.hpp"
#include <vector>

namespace Ajardrez
{

    bool MoveGen::is_square_attacked(const Board& board, int sq, Color attacker_color) {
        int r = row(sq);
        int c = col(sq);

        int pawn_dir = (attacker_color == Color::WHITE) ? -1 : 1;
        for (int dc : {-1, 1}) {
            int pr = r + pawn_dir;
            int pc = c + dc;
            if (pr >= 0 && pr < 8 && pc >= 0 && pc < 8) {
                Piece p = board.get_piece(pr * 8 + pc);
                if (p.type == PieceType::PAWN && p.color == attacker_color) return true;
            }
        }

        int dr_k[] = {-2, -2, -1, -1, 1, 1, 2, 2};
        int dc_k[] = {-1, 1, -2, 2, -2, 2, -1, 1};
        for (int i = 0; i < 8; ++i) {
            int nr = r + dr_k[i];
            int nc = c + dc_k[i];
            if (nr >= 0 && nr < 8 && nc >= 0 && nc < 8) {
                Piece p = board.get_piece(nr * 8 + nc);
                if (p.type == PieceType::KNIGHT && p.color == attacker_color) return true;
            }
        }

        int straight_dirs[4][2] = {{1,0}, {-1,0}, {0,1}, {0,-1}};
        for (auto& dir : straight_dirs) {
            int nr = r + dir[0], nc = c + dir[1];
            while (nr >= 0 && nr < 8 && nc >= 0 && nc < 8) {
                Piece p = board.get_piece(nr * 8 + nc);
                if (p.type != PieceType::NONE) {
                    if (p.color == attacker_color && (p.type == PieceType::ROOK || p.type == PieceType::QUEEN)) return true;
                    break;
                }
                nr += dir[0]; nc += dir[1];
            }
        }

        int diag_dirs[4][2] = {{1,1}, {1,-1}, {-1,1}, {-1,-1}};
        for (auto& dir : diag_dirs) {
            int nr = r + dir[0], nc = c + dir[1];
            while (nr >= 0 && nr < 8 && nc >= 0 && nc < 8) {
                Piece p = board.get_piece(nr * 8 + nc);
                if (p.type != PieceType::NONE) {
                    if (p.color == attacker_color && (p.type == PieceType::BISHOP || p.type == PieceType::QUEEN)) return true;
                    break;
                }
                nr += dir[0]; nc += dir[1];
            }
        }

        for (int dr = -1; dr <= 1; ++dr) {
            for (int dc = -1; dc <= 1; ++dc) {
                if (dr == 0 && dc == 0) continue;
                int nr = r + dr, nc = c + dc;
                if (nr >= 0 && nr < 8 && nc >= 0 && nc < 8) {
                    Piece p = board.get_piece(nr * 8 + nc);
                    if (p.type == PieceType::KING && p.color == attacker_color) return true;
                }
            }
        }
        return false;
    }

    bool MoveGen::is_in_check(const Board& board, Color color) {
        int king_sq = board.find_king_square(color);
        if (king_sq == -1) return false;
        Color enemy = (color == Color::WHITE) ? Color::BLACK : Color::WHITE;
        return is_square_attacked(board, king_sq, enemy);
    }

    std::vector<Move> MoveGen::get_pseudo_legal_moves(const Board& board, int from) {
        std::vector<Move> moves;
        Piece p = board.get_piece(from);
        if (p.color != board.get_turn() || p.type == PieceType::NONE) return moves;

        int r = row(from);
        int c = col(from);

        // Peones
        if (p.type == PieceType::PAWN) {
            int dir = (p.color == Color::WHITE) ? 1 : -1;
            int next_sq = from + dir * 8;
            if (is_valid(next_sq) && board.get_piece(next_sq).type == PieceType::NONE) {
                moves.emplace_back(from, next_sq, board.get_piece(next_sq));
                int start_row = (p.color == Color::WHITE) ? 1 : 6;
                int double_sq = from + dir * 16;
                if (r == start_row && board.get_piece(double_sq).type == PieceType::NONE) {
                    moves.emplace_back(from, double_sq, board.get_piece(double_sq));
                }
            }
            for (int dc : {-1, 1}) {
                if (c + dc >= 0 && c + dc < 8) {
                    int cap_sq = from + dir * 8 + dc;
                    if (is_valid(cap_sq) && board.get_piece(cap_sq).color != Color::NONE && board.get_piece(cap_sq).color != p.color) {
                        moves.emplace_back(from, cap_sq, board.get_piece(cap_sq));
                    }
                }
            }
        }

        if (p.type == PieceType::KNIGHT) {
            int dr[] = {-2, -2, -1, -1, 1, 1, 2, 2};
            int dc[] = {-1, 1, -2, 2, -2, 2, -1, 1};
            for (int i = 0; i < 8; ++i) {
                int nr = r + dr[i];
                int nc = c + dc[i];
                if (nr >= 0 && nr < 8 && nc >= 0 && nc < 8) {
                    int target = nr * 8 + nc;
                    if (board.get_piece(target).color != p.color) {
                        moves.emplace_back(from, target, board.get_piece(target));
                    }
                }
            }
        }

        auto add_sliding_moves = [&](const std::vector<std::pair<int, int>>& directions) {
            for (auto [dr, dc] : directions) {
                int nr = r + dr;
                int nc = c + dc;
                while (nr >= 0 && nr < 8 && nc >= 0 && nc < 8) {
                    int target = nr * 8 + nc;
                    Piece target_p = board.get_piece(target);
                    if (target_p.color == Color::NONE) {
                        moves.emplace_back(from, target, target_p);
                    } else {
                        if (target_p.color != p.color) {
                            moves.emplace_back(from, target, target_p);
                        }
                        break;
                    }
                    nr += dr;
                    nc += dc;
                }
            }
        };

        if (p.type == PieceType::ROOK || p.type == PieceType::QUEEN) {
            add_sliding_moves({{1, 0}, {-1, 0}, {0, 1}, {0, -1}});
        }
        if (p.type == PieceType::BISHOP || p.type == PieceType::QUEEN) {
            add_sliding_moves({{1, 1}, {1, -1}, {-1, 1}, {-1, -1}});
        }

        if (p.type == PieceType::KING) {
            for (int dr = -1; dr <= 1; ++dr) {
                for (int dc = -1; dc <= 1; ++dc) {
                    if (dr == 0 && dc == 0) continue;
                    int nr = r + dr;
                    int nc = c + dc;
                    if (nr >= 0 && nr < 8 && nc >= 0 && nc < 8) {
                        int target = nr * 8 + nc;
                        if (board.get_piece(target).color != p.color) {
                            moves.emplace_back(from, target, board.get_piece(target));
                        }
                    }
                }
            }
        }

        return moves;
    }

    std::vector<Move> MoveGen::get_legal_moves(Board& board, int from) {
        std::vector<Move> pseudo = get_pseudo_legal_moves(board, from);
        std::vector<Move> legal;
        Color side = board.get_piece(from).color;

        for (const auto& m : pseudo) {
            board.make_move(m);
            if (!is_in_check(board, side)) {
                legal.push_back(m);
            }
            board.undo_move();
        }
        return legal;
    }

    std::vector<Move> MoveGen::generate_all_legal_moves(Board& board, Color side) {
        std::vector<Move> all_legal;
        for (int i = 0; i < 64; ++i) {
            if (board.get_piece(i).color == side) {
                auto moves = get_legal_moves(board, i);
                all_legal.insert(all_legal.end(), moves.begin(), moves.end());
            }
        }
        return all_legal;
    }

} // namespace Ajardrez