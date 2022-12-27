function [M] = assemble_neigh(M,row,neight,M1,nln,n_edge)
%% [M] = assemble_neigh(M,row,neight,M1,nln,n_edge)
%==========================================================================
% Assembly of the local matrices for the neighbouring element
%==========================================================================
%    called in main2D.m
%
%    INPUT:
%          M           : (array real)  input matrix M
%          row         : (int)         row index
%          neigh       : (int)         neighbours structure
%          M1          : (array real)  matrix with only contribution from
%                                      neighbouring elements
%          nln         : (int)         local dof
%          n_edge      : (int)         edge number
%    OUTPUT:
%          M           : (array real)  assembled matrix M

for iedg = 1 : n_edge
    if neight(iedg) ~= -1
        j = (neight(iedg)-1)*nln*ones(nln,1) + [1:nln]';
        M(row,j) =M(row,j) + M1(:,:,iedg);
    end
end
