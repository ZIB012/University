function [dphiq, Grad, B_edge, G_edge]= evalshape(shape_basis,nodes_2D,nodes_1D,nln)
%% [dphiq, Grad, B_edge, G_edge]= evalshape(shape_basis,nodes_2D,nodes_1D,nln);
% PURPOSE:
%
% This routine evaluates the scalar shape functions and their derivatives
% at the quadrature nodes on the reference element, and on the boundary
% edges of the reference element, i.e.,
%
% dphiq  -> scalar shape functions (volume integrals)
% B_edge -> scalar shape functions on the faces (face integrals)
% Grad   -> grandient of the scalar shape functions (volume integrals)
% G_edge -> grandient of the scalar shape functions on the faces (face
%           integrals).
%
% Author:
% Paola Antonietti
%--------------------------------------------------------------------


nqn_2D = length(nodes_2D);
nqn_1D = length(nodes_1D);

c=shape_basis(1).coeff;

for s=1:nln % loop over the number of scalar shape functions
    
    csi=nodes_2D(:,1);
    eta=nodes_2D(:,2);
    dphiq(1,:,s)=eval(shape_basis(s).fbasis);
    Grad(:,1,s)=eval(shape_basis(s).Gbasis_1);
    Grad(:,2,s)=eval(shape_basis(s).Gbasis_2);
    
    % Edge 1
    csi=nodes_1D;
    eta=-zeros(1,nqn_1D);
    B_edge(s,:,1)=eval(shape_basis(s).fbasis);
    G_edge(:,1,s,1) = eval(shape_basis(s).Gbasis_1)';
    G_edge(:,2,s,1) = eval(shape_basis(s).Gbasis_2)';
    
    % Edge 2
    eta=nodes_1D;
    csi=ones(1,nqn_1D)-eta;
    B_edge(s,:,2)=eval(shape_basis(s).fbasis);
    G_edge(:,1,s,2) = eval(shape_basis(s).Gbasis_1)';
    G_edge(:,2,s,2) = eval(shape_basis(s).Gbasis_2)';
    
    
    % Edge 3
    csi=zeros(1,nqn_1D);
    eta=ones(1,nqn_1D)-nodes_1D;
    B_edge(s,:,3)=eval(shape_basis(s).fbasis);
    G_edge(:,1,s,3) = eval(shape_basis(s).Gbasis_1)';
    G_edge(:,2,s,3) = eval(shape_basis(s).Gbasis_2)';
    
end