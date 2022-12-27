function S = matrix_S(fem,femregion,neighbour,Data,t)
%% S = matrix_S(femregion,neighbour,Dati)
%==========================================================================
% Assembly of matrix S
%==========================================================================
%    called in main2D.m
%
%    INPUT:
%          Dati        : (struct)  see dati.m
%          femregion   : (struct)  see create_dof.m
%          neighbour   : (struct)  see neighbours.m
%    OUTPUT:
%          S           : (matrix)

addpath FESpace
addpath Assembly

fprintf('============================================================\n')
fprintf('--------Begin computing matrix S for %s\n',Data.method);
fprintf('============================================================\n')

% shape functions
[shape_basis] = basis_lagrange(fem);

% quadrature nodes and weights for integrals
[nodes_1D, w_1D, nodes_2D, ~] = quadrature(Data.nqn);
nqn_1D = length(w_1D);

% evaluation of shape functions on quadrature poiint
[~, ~, B_edge, ~] = evalshape(shape_basis,nodes_2D,nodes_1D,femregion.nln);

% definition of penalty coefficient (note that is scaled only
% wrt the polynomial degree
penalty_coeff=Data.penalty_coeff.*(femregion.degree.^2);



% Assembly begin ...
S=sparse(femregion.ndof,femregion.ndof);  % \int_{E_h} penalty  h_e^(-1) [v].[u] ds

% loop over elements
for ie = 1:femregion.ne
    
    % Local to global map --> To be used in the assembly phase
    index = (ie-1)*femregion.nln*ones(femregion.nln,1) + [1:femregion.nln]';
    
    % Index of the current edges
    index_element = femregion.nedges*(ie-1).*ones(femregion.nedges,1) + [1:1:femregion.nedges]';
    
    % Find neighbouring elements (through structure nieghbour)
    neigh_ie = neighbour.neigh(ie,:);
    neighedges_ie = neighbour.neighedges(ie,:);
    
    % Coordinates of the verteces of the current triangle
    coords_elem = femregion.coords_element(index_element, :);
    
    % compute normals to the edges
    [normals,meshsize] = get_normals_meshsize_faces(coords_elem);
    
    % =====================================================================
    % Compute integrals over edges
    % =====================================================================
    SN = zeros(femregion.nln,femregion.nln,neighbour.nedges);
    
    for iedg = 1 : neighbour.nedges % Loop over the triangle's  edges
        
        
        % index of neighbour edge
        neigedge = neighedges_ie(iedg);    
        
        % scaling of the penalty coefficient wrt the mesh size
        penalty_scaled = penalty_coeff./meshsize(iedg); 
        
        
        % assembly of interface matrices 
        for k = 1:nqn_1D   % loop over 1D quadrature nodes
            
            % scaled weight for the quadrature formula
            ds = meshsize(iedg)*w_1D(k);
            kk = nqn_1D+1-k;  % index of neighbouring quad point
            
            for i = 1:femregion.nln % loop over local dof              
                for j = 1 : femregion.nln % loop over local dof
                    % Internal faces                
                    if neigh_ie(iedg) ~= -1 
                        % S --> \int_{E_h} penalty  h_e^(-1) [v].[u] ds
                        S(index(i),index(j)) = S(index(i),index(j)) ...
                                        + penalty_scaled .* B_edge(i,k,iedg) .* B_edge(j,k,iedg) .* ds;
                        
                        SN(i,j,iedg) = SN(i,j,iedg) ...
                                   - penalty_scaled .* B_edge(i,k,iedg) .* B_edge(j,kk,neigedge) .* ds;
                               
                    % Boundary faces   
                    elseif neigh_ie(iedg) == -1 
%                         %
%                         IB(index(i),index(j))  = IB(index(i),index(j)) ...
%                                    +  ((G_edge(k,:,i,iedg)*BJinv)*normals(:,iedg)) .* B_edge(j,k,iedg) .* ds;
                    end
                end
                
                
            end
        end
    end
    
    % Assembly phase
    [S] = assemble_neigh(S,index,neigh_ie,SN,femregion.nln,neighbour.nedges); 
end

fprintf('--------End computing matrix S for %s \n',Data.method);




