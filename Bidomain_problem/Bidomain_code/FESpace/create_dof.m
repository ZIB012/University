function [femregion]=create_dof(Dati,region)
%% [femregion]=create_dof(Dati,region)
% PURPOSE:
%
% This routine generates a structure containing all the information of the
% DG finite element space, i.e.,
%
% femregion.fem -> finite element space
% femregion.domain -> coordinates of the (square) domain
% femregion.h -> mesh size
% femregion.nln -> number of local nodes
% femregion.ndof -> total number of degrees of freedom
% femregion.ne -> total number of elements
% femregion.dof -> coordinates of the degrees of freedom
% femregion.nqn -> number of 1D quadrature nodes
% femregion.degree -> polynomial degree
% femregion.coord -> coordinates of the nodes of the mesh
% femregion.connectivity -> connectivity matrix for the mesh
% femregion.coords_element -> coordinates of the P1-DG degrees of freedom
% femregion.boundary_edges -> boundary edges
%
% Author:
% Paola Antonietti
%--------------------------------------------------------------------




fprintf('Computing dof for elements %s\n',Dati.fem);

nln=(region.degree+1).*(region.degree+2)./2;
ne=region.ne;
nedge=region.nedge;

dof_hat =[];

switch Dati.fem  % dof on the reference element

    case{'P1','D1'}
        degree=1;
        dof_hat =[ 0 1 0
                   0 0 1]';

    case{'P2','D2'}
        degree=2;
        dof_hat =[ 0    0.5     1   0.5     0   0
                   0    0       0   0.5     1   0.5]';
               
    case{'P3','D3'}
        degree=3;
        dof_hat =[ 0   0.25   0.75  1   0.75  0.25  0  0    0    1/3
                   0   0      0     0   0.25  0.75  1  0.75 0.25 1/3]';               
               
               
end


dof=[];

for ie=1:ne
    index_el=nedge*(ie-1).*ones(1,nedge) + [1:1:nedge];
    [~ , ~ , temp] = get_jacobian_physical_points(region.coords_element(index_el, :), dof_hat);
    dof=[dof; temp];
end

    

femregion=struct('fem',Dati.fem,...
    'domain',region.domain,...
    'h',region.h,...
    'nedges', region.nedge,...
    'nln',nln,...
    'ndof',nln*ne,...
    'ne',ne,...
    'dof',dof,...
    'nqn',Dati.nqn,...
    'degree',degree,...
    'coord',region.coord,...
    'connectivity',region.connectivity,...
    'coords_element', region.coords_element,...
    'boundary_edges',region.boundary_edges);

