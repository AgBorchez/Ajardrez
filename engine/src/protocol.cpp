#include "protocol.hpp"
#include "movegen.hpp"
#include "search.hpp"
#include <iostream>
#include <sstream>

namespace Ajardrez
{
    Protocol::Protocol() {
        board.reset();
    }

    std::string Protocol::process_command(const std::string& line) {
        std::istringstream iss(line);
        std::string command;
        iss >> command;

        if (command == "PING") {
            return "PONG";
        }
        else if (command == "NEW_GAME") {
            board.reset();
            return "OK_NEW_GAME";
        }
        else if (command == "GET_LEGAL_MOVES") {
            std::string sq_str;
            iss >> sq_str;
            int sq = from_string(sq_str);
            if (sq == -1) return "MOVES ";

            auto moves = MoveGen::get_legal_moves(board, sq);
            std::string res = "MOVES ";
            for (size_t i = 0; i < moves.size(); ++i) {
                res += to_string(moves[i].from) + to_string(moves[i].to);
                if (i + 1 < moves.size()) res += ",";
            }
            return res;
        }
        else if (command == "MAKE_MOVE") {
            std::string move_str;
            iss >> move_str;
            if (move_str.length() >= 4) {
                int from = from_string(move_str.substr(0, 2));
                int to = from_string(move_str.substr(2, 2));

                PieceType promo = PieceType::QUEEN;
                if (move_str.length() == 5) {
                    char p = move_str[4];
                    if (p == 'q' || p == 'Q') promo = PieceType::QUEEN;
                    else if (p == 'r' || p == 'R') promo = PieceType::ROOK;
                    else if (p == 'b' || p == 'B') promo = PieceType::BISHOP;
                    else if (p == 'n' || p == 'N') promo = PieceType::KNIGHT;
                }

                auto legals = MoveGen::get_legal_moves(board, from);
                Move selected_move(from, to, board.get_piece(to));
                
                for (const auto& m : legals) {
                    if (m.to == to) {
                        selected_move = m;
                        if (m.type == MoveType::PROMOTION) {
                            selected_move.promotion_piece = promo;
                        }
                        break;
                    }
                }

                board.make_move(selected_move);
                return "OK_MOVE " + move_str;
            }
            return "ERROR_MOVE";
        }
        else if (command == "UNDO_MOVE")
        {
            if (board.undo_move())
            {
                return "OK_UNDO";
            }
            return "ERR_UNDO";
            
        }
        
        else if (command == "CALCULATE_AI") {
            int depth = 4;
            Move best = Search::find_best_move(board, depth);
            if (best.from != -1) {
                return "BESTMOVE " + to_string(best.from) + to_string(best.to);
            }
            return "NOMOVE";
        }
        else if (command == "QUIT") {
            std::cout << "BYE" << std::endl;
            exit(0);
        }
        else if (command == "GET_STATUS") {
            Color current = board.get_turn();
            auto legal_moves = MoveGen::generate_all_legal_moves(board, current);
            
            if (legal_moves.empty()) {
                if (MoveGen::is_in_check(board, current)) {
                    return "STATUS CHECKMATE";
                } else {
                    return "STATUS STALEMATE"; // Tablas por rey ahogado
                }
            }
            return "STATUS ONGOING";
        }

        return "UNKNOWN_COMMAND";
    }

    void Protocol::run() {
        std::string line;
        while (std::getline(std::cin, line)) {
            if (line.empty()) continue;
            std::string response = process_command(line);
            std::cout << response << std::endl;
        }
    }

} // namespace Ajardrez