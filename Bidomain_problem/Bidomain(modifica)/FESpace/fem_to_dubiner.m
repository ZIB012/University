function [u0] = fem_to_dubiner (uh, femregion, Data)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%Function to convert the components of a vector with respect to the FEM
%basis to components with respect to Dubiner basis
%Input: uh (components wrt FEM basis), femregion and Data
%Output: u0 (components wrt Dubiner basis)
%
% Federica Botta, Matteo Calafà
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


% quadrature nodes and weights for integrals
[nodes_1D, ~, nodes_2D, w_2D] = quadrature(Data.nqn);

% evaluation of shape functions on quadrature point both on FEM basis and
% Dubiner basis
[shape_basis] = basis_legendre_dubiner(femregion.fem);
[phi_dub, ~, ~, ~] = evalshape_tria_dubiner(shape_basis,nodes_2D, nodes_1D,Data.nqn,femregion.nln);
[shape_basis] = basis_lagrange(append("P", femregion.fem(2)));
[phi_fem, ~, ~, ~] = evalshape(shape_basis,nodes_2D,nodes_1D,femregion.nln);

u0 = zeros(femregion.ndof,1);

for ie = 1:femregion.ne
    index = (ie-1)*femregion.nln*ones(femregion.nln,1) + [1:femregion.nln]';
    for i = 1 : femregion.nln
        for k = 1:length(w_2D) 
            uh_eval_k = 0;
            for j = 1:femregion.nln
                uh_eval_k = uh_eval_k + uh(index(j))*phi_fem(1,k,j);
            end
            u0(index(i)) = u0(index(i)) + uh_eval_k*phi_dub(1,k,i).*w_2D(k);
        end
    end    
end