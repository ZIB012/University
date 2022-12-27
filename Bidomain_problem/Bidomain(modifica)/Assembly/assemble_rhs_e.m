function [ f ] = assemble_rhs_e(femregion,neighbour,Data,t)
%% [f] =  assemble_rhs_e(femregion,neighbour,Data,t)
%==========================================================================
% Assembly the rhs for the equation associated to the extracellular
% potential
%==========================================================================
%    called in main2D.m
%
%    INPUT:
%          Dati        : (struct)  see dati.m
%          femregion   : (struct)  see create_dof.m
%          neighbour   : (struct)  see neighbours.m
%          t           : (real)    instant of time;
%    OUTPUT:
%          f           : (array real) rhs of Phi_e equation;

addpath FESpace
addpath Assembly


% shape functions
if (Data.fem(1) == 'D')
    [shape_basis] = basis_legendre_dubiner(femregion.fem);
else
    [shape_basis] = basis_lagrange(femregion.fem);
end


% quadrature nodes and weights for integrals
[nodes_1D, w_1D, nodes_2D, w_2D] = quadrature(Data.nqn);
nqn_1D = length(w_1D);

% evaluation of shape functions on quadrature poiint
if (Data.fem(1) == 'D')
    [dphiq, ~ , B_edge, ~ ] = evalshape_tria_dubiner(shape_basis,nodes_2D,nodes_1D,nqn_1D,femregion.nln);
else
    [dphiq, ~ , B_edge, ~ ] = evalshape(shape_basis,nodes_2D,nodes_1D,femregion.nln);
end

% definition of penalty coefficient (note that is scaled only
% wrt the polynomial degree
penalty_coeff=Data.penalty_coeff.*(femregion.degree.^2);


% Define parameters in order to evaluate the forcing term
a= Data.a;
ChiM=Data.ChiM;
Cm=Data.Cm;
kappa=Data.kappa;
epsilon=Data.epsilon;
gamma=Data.gamma;
sigma_e = Data.Sigma_e;
b=Data.b;

% Assembly begin ...

f=sparse(femregion.ndof,1);  % \int_{\Omega} f . v dx + boundary conditions

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
    
    [BJ,~  , pphys_2D] = get_jacobian_physical_points(coords_elem, nodes_2D);
    
    % quadrature nodes on the edges (physical coordinates)
    [pphys_1D] = get_physical_points_faces(coords_elem, nodes_1D);
    
    % compute normals to the edges
    [~ ,meshsize] = get_normals_meshsize_faces(coords_elem);
    
    % =====================================================================
    % Compute integrals over triangles
    % =====================================================================
    for k = 1:length(w_2D) % loop over 2D quadrature nodes
        
        % scaled weight for the quadrature formula
        dx = w_2D(k)*det(BJ);
 
        % evaluation of the load term       
        x = pphys_2D(k,1);
        y = pphys_2D(k,2);
        F = eval(Data.source_e);
        for i = 1 : femregion.nln % assembly load vector
            f(index(i)) = f(index(i)) - F*dphiq(1,k,i).*dx;
        end
    end
    
    % =====================================================================
    % Compute integrals over edges
    % =====================================================================
    
    % Loop over the triangle's  edges
    for iedg = 1 : neighbour.nedges 
        
        
        % index of neighbour edge
        neigedge = neighedges_ie(iedg);    
        
        % scaling of the penalty coefficient wrt the mesh size
        penalty_scaled = penalty_coeff./meshsize(iedg); 
        
        
        % assembly of interface matrices 
        for k = 1:nqn_1D   % loop over 1D quadrature nodes
            % scaled weight for the quadrature formula
            ds = meshsize(iedg)*w_1D(k);
            for i = 1:femregion.nln % loop over local dof              
                if  neigh_ie(iedg) == -1  % Update the forcing term with boundary conditions
                    x=pphys_1D(k,1,iedg);
                    y=pphys_1D(k,2,iedg);
                    g=eval(Data.Neumann_e);
                    f(index(i)) = f(index(i)) +  B_edge(i,k,iedg) .* g .* ds ;
                end


        end
    end  
end
end

