%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% It generates the basis functions of legendre Qp--Pp on the reference
% element [-1,1]X[-1,1]
%
% INPUT: type of basis function to use 
% OUTPUT: the structure containing the number of basis functions and their
% analytic expression
% 
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function [shape_basis]=basis_legendre_dubiner(fem)


fem=char(fem);

type_elem=fem(1);
degree=sscanf(fem(2:end),'%f');
switch type_elem
    case{'P','D'}
        nln=0.5.*(degree+1).*(degree+2);
        n_edge=3;
    case{'Q'}
        nln=(degree+1).^2;
        n_edge=4;
end

shape_basis=struct('nln',nln,...
             'n_edge',n_edge,...
             'deg',degree);   



