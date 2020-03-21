package uk.ac.nott.cs.g53dia.multilibrary;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * A simple user interface for watching an individual LitterAgent.
 *
 * @author Neil Madden.
 */
/*
 * Copyright (c) 2003 Stuart Reeves Copyright (c) 2003-2005 Neil Madden
 * (nem@cs.nott.ac.uk). Copyright (c) 2011 Julian Zappala (jxz@cs.nott.ac.uk).
 * See the file "license.terms" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
public class MASViewer extends JFrame implements ActionListener {
	
	private static final long serialVersionUID = -2810783821678793885L;
	final static int SIZE = (LitterAgent.VIEW_RANGE * 2) + 1, ICON_SIZE = 25, PSIZE = SIZE * ICON_SIZE;

    LitterAgentViewerIconFactory iconfactory;
	JLabel[][] cells, agents;
	JLabel tstep, charge, pos, waste, disposed, score;
	JLayeredPane lp;
	JPanel pCells;
	JPanel pAgents;
	JPanel infop;
	JComboBox<String> agentList;
	MAS mas;
	LitterAgent agt;

	
	public MASViewer(LitterAgent agt) {
		this(agt, new DefaultLitterAgentViewerIconFactory());
	}

	public MASViewer(LitterAgent agt, LitterAgentViewerIconFactory fac) {
		this.agt= agt;
		this.iconfactory = fac;
		Container c = getContentPane();
		c.setLayout(new BorderLayout());

		// Create the cell viewer
		cells = new JLabel[SIZE][SIZE];
		agents = new JLabel[SIZE][SIZE];
		lp = new JLayeredPane();
		lp.setSize(new Dimension(PSIZE,PSIZE));
		pCells = new JPanel(new GridLayout(SIZE, SIZE));
		pCells.setBackground(Color.WHITE);
		pAgents = new JPanel(new GridLayout(SIZE, SIZE));
		pAgents.setOpaque(false);

		for (int y = 0; y < SIZE; y++) {
			for (int x = 0; x < SIZE; x++) {
				cells[x][y] = new JLabel();
				pCells.add(cells[x][y]);

				agents[x][y] = new JLabel(iconfactory.getIconForLitterAgent(agt));
				agents[x][y].setBounds(PSIZE/2 - ICON_SIZE/2,PSIZE/2 - ICON_SIZE/2,ICON_SIZE,ICON_SIZE);
				agents[x][y].setVisible(false);
				pAgents.add(agents[x][y]);
			}
		}

		lp.add(pCells,new Integer(0));
		lp.add(pAgents,new Integer(1));
		pCells.setBounds(0,0,PSIZE,PSIZE);
		pAgents.setBounds(0,0,PSIZE,PSIZE);
		c.add(lp, BorderLayout.CENTER);
		
		// Create some labels to show info about the LitterAgent and environment
		infop = new JPanel(new GridLayout(0, 4));
		infop.add(new JLabel("Timestep:"));
		tstep = new JLabel("0");
		infop.add(tstep);
		infop.add(new JLabel("Charge:"));
		charge = new JLabel("200");
		infop.add(charge);
		infop.add(new JLabel("Position:"));
		pos = new JLabel("(0,0)");
		infop.add(pos);
		infop.add(new JLabel("Score:"));
		score = new JLabel("0");
		infop.add(score);
			

		c.add(infop, BorderLayout.SOUTH);
		//infop.setPreferredSize(new Dimension(200,100));

		setSize(PSIZE,PSIZE + 50);
		setTitle("MAS Viewer");
		setVisible(true);
	}
	
	public MASViewer(MAS mas) {
		this(mas.get(0));
		this.mas = mas;

		String[] agentNames = new String[mas.size()];
		for (int i = 0; i < mas.size(); i++) {
			agentNames[i] = "Agent " + i;
		}

		//A drop down list to select which tanker to view
		agentList = new JComboBox<String>(agentNames);
		infop.add(agentList);

		//Event handler for drop down list
		agentList.addActionListener(this);

	}


	public void setAgent(LitterAgent agt) {
		this.agt= agt;
	}

	public void tick(Environment env) {
		Cell[][] view = env.getView(agt.getPosition(),LitterAgent.VIEW_RANGE);
		pos.setText(agt.getPosition().toString());
		tstep.setText(new String(""+env.getTimestep()));
		charge.setText(new String(""+agt.getChargeLevel()));
		score.setText("" + mas.getScore());
		for (int x = 0; x < SIZE; x++) {
			for (int y = 0; y < SIZE; y++) {
				Icon cur = iconfactory.getIconForCell(view[x][y]);
				cells[x][y].setIcon(cur);
				agents[x][y].setVisible(false);
				// Now draw agents
				for (LitterAgent a : mas) {
					if (view[x][y].getPoint().equals(a.getPosition())) {
						agents[x][y].setVisible(true);
					}	
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		agt= mas.get(agentList.getSelectedIndex());

	}
	
}
