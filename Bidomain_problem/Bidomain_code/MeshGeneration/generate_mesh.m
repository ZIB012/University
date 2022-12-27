function [region]=generate_mesh(Data,step_refinement)
%% [region]=generate_mesh(Data,step_refinement)
%==========================================================================
% Creates triangular or quadrilateral mesh
%==========================================================================
%    called in main2D.m
%
%    INPUT:
%          Data           : (struct)  see dati.m
%          step_refinement: (int) number of refinement level
%
%    OUTPUT:
%          Region      : (struct) having fields: dim
%                                                domain
%                                                h
%                                                coord
%                                                connectivity
%                                                coords_element
%                                                boundary_edges 
%                                                degree


% domain
x0=Data.domain(1,1);
x1=Data.domain(1,2);
y0=Data.domain(2,1);
y1=Data.domain(2,2);

% geometry
g=[
    2     2     2     2
    x0    x1    x1    x0
    x1    x1    x0    x0
    y1    y1    y0    y0
    y1    y0    y0    y1
    0     0     0     0
    1     1     1     1
    ];

% points
coord=[
    x0     x1     x1    x0
    y1     y1     y0    y0
    ];

% boundary edges
boundary_edge=[
    1     2     3     4
    2     3     4     1
    0     0     0     0
    1     1     1     1
    1     2     3     4
    0     0     0     0
    1     1     1     1
    ];

connectivity =[
    1     2
    4     1
    3     3
    1     1
    ];

nedge=3;


for i= 1:step_refinement % loop over the number of refinements
    [coord, boundary_edge, connectivity]=refine_mesh(g,coord, boundary_edge, connectivity,'regular');
    coord=jiggle_mesh(coord, boundary_edge, connectivity);
end

ne=size(connectivity,2);
h=1/sqrt(ne);

coords_element=[]; % coordinates of the elements (counted with their multiplicity)
for ie=1:ne
    for k=1:nedge
        coords_element=[coords_element; coord(1,connectivity(k,ie)),coord(2,connectivity(k,ie))];
    end
end


region=struct('dim',2,...
    'domain',Data.domain,...
    'nedge', nedge,...
    'h',h,...
    'nvert',size(coord,2),...
    'ne',ne,...
    'coord',coord',...
    'boundary_edges',boundary_edge,...
    'connectivity',connectivity,...
    'coords_element',coords_element,...
    'degree', str2num(Data.fem(2)));

