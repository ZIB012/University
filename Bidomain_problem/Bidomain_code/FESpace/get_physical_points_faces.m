function [pphys_1D] = get_physical_points_faces(loc_coord, node_1D)
%% [pphys_1D] = get_physical_points_faces(loc_coord, node_1D)
% 
%  PURPOSE:
%      Compute the image of an egde quadrature point on the physical domain 



nqn_1D=length(node_1D);
nfaces=length(loc_coord(:,1));
nodes_face =zeros(nqn_1D,2,nfaces);

x0=loc_coord(1,1);   % x-coordinates of vertices
x1=loc_coord(2,1);
x2=loc_coord(3,1);

y0=loc_coord(1,2);   % y-coordinates of vertices
y1=loc_coord(2,2);
y2=loc_coord(3,2);

nodes_face(:,:,1)=[node_1D;zeros(1,nqn_1D)]';
nodes_face(:,:,2)=[node_1D;ones(1,nqn_1D)-node_1D]';
nodes_face(:,:,3)=[zeros(1,nqn_1D);ones(1,nqn_1D)-node_1D]';


for iedg = 1:nfaces
    
    BJ_face(:,:,iedg) = [x1-x0,x2-x0;y1-y0,y2-y0];       % Jacobian of elemental map
    BJ_face_inv(:,:,iedg) = inv(BJ_face(:,:,iedg));    % inverse of Jacobian of elemental map
    trans=[x0;y0];                      % translation vector    
    
    for k=1:nqn_1D
        pphys_1D(k,:,iedg)=transpose((BJ_face(:,:,iedg)*transpose(nodes_face(k,:,iedg))+trans));
    end
    
end


