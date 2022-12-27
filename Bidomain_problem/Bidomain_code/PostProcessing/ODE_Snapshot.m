function ODE_Snapshot(femregion,Data,u_h,t)



x=femregion.dof(:,1);
y=femregion.dof(:,2);

figure;
x1=femregion.domain(1,1);
x2=femregion.domain(1,2);
y1=femregion.domain(2,1);
y2=femregion.domain(2,2);
M=max(u_h);
m=min(u_h);
if (abs(m-M) < 0.1)
    M=m+1;
end


if Data.fem(2) == '1'
    
    k = 1;
    for ie = 1 : femregion.ne
        trisurf([1 2 3],femregion.dof(k:k+2,1),femregion.dof(k:k+2,2),full(u_h(k:k+2)))
        hold on;
        k=k+3;
    end
elseif Data.fem(2) == '2'
    k = 1;
    for ie = 1 : femregion.ne
        trisurf([1 2 3],femregion.dof([k,k+2,k+4],1),femregion.dof([k,k+2,k+4],2),full(u_h([k,k+2,k+4])))
        hold on;
        k=k+6;
    end   
    
elseif Data.fem(2) == '3'
    k = 1;
    for ie = 1 : femregion.ne
        trisurf([1 2 3],femregion.dof([k,k+3,k+6],1),femregion.dof([k,k+3,k+6],2),full(u_h([k,k+3,k+6])))
        hold on;
        k=k+10;
    end  
end

title(['u_h(x,y) at time:' num2str(t)]); xlabel('x-axis'); ylabel('y-axis');
axis([x1,x2,y1,y2,-1,1]); colorbar;
pause(0.1);







