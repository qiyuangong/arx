/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deidentifier.arx.gui.view.impl.utility;

import java.text.DecimalFormat;

import org.deidentifier.arx.aggregates.StatisticsBuilderInterruptible;
import org.deidentifier.arx.aggregates.StatisticsEquivalenceClasses;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.model.ModelEvent.ModelPart;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.common.ClipboardHandlerTable;
import org.deidentifier.arx.gui.view.impl.common.async.Analysis;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisContext;
import org.deidentifier.arx.gui.view.impl.common.async.AnalysisManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import de.linearbits.swt.table.DynamicTable;
import de.linearbits.swt.table.DynamicTableColumn;

/**
 * This view displays statistics about the distribution of equivalence classes
 *
 * @author Fabian Prasser
 */
public class ViewStatisticsEquivalenceClassTable extends ViewStatistics<AnalysisContextVisualizationDistribution> {

    /** View */
    private Composite                  root;

    /** View */
    private DynamicTable               table;

    /** Internal stuff. */
    private AnalysisManager            manager;

    private static final DecimalFormat format = new DecimalFormat("0.####");

    /**
     * Creates a new instance.
     *
     * @param parent
     * @param controller
     * @param target
     * @param reset
     */
    public ViewStatisticsEquivalenceClassTable(final Composite parent,
                                     final Controller controller,
                                     final ModelPart target,
                                     final ModelPart reset) {
        
        super(parent, controller, target, reset, false);
        this.manager = new AnalysisManager(parent.getDisplay());
    }
    
    @Override
    protected Control createControl(Composite parent) {

        this.root = new Composite(parent, SWT.NONE);
        this.root.setLayout(new FillLayout());
        this.table = SWTUtil.createTableDynamic(root, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        this.table.setHeaderVisible(true);
        this.table.setLinesVisible(true);
        this.table.setMenu(new ClipboardHandlerTable(table).getMenu());

        String size = "50%";
        if (super.getTarget() == ModelPart.OUTPUT) {
            size = "33%";
        }
        
        DynamicTableColumn c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth(size, "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("EquivalenceClassStatistics.1")); //$NON-NLS-1$
        c = new DynamicTableColumn(table, SWT.LEFT);
        c.setWidth(size, "100px"); //$NON-NLS-1$ //$NON-NLS-2$
        c.setText(Resources.getMessage("EquivalenceClassStatistics.2")); //$NON-NLS-1$
        if (super.getTarget() == ModelPart.OUTPUT) {
            c = new DynamicTableColumn(table, SWT.LEFT);
            c.setWidth(size, "100px"); //$NON-NLS-1$ //$NON-NLS-2$
            c.setText(Resources.getMessage("EquivalenceClassStatistics.3")); //$NON-NLS-1$
        }
        for (final TableColumn col : table.getColumns()) {
            col.pack();
        }
        
        SWTUtil.createGenericTooltip(table);
        return root;
    }

    @Override
    protected AnalysisContextVisualizationDistribution createViewConfig(AnalysisContext context) {
        return new AnalysisContextVisualizationDistribution(context);
    }

    @Override
    protected void doReset() {
        table.setRedraw(false);
        for (final TableItem i : table.getItems()) {
            i.dispose();
        }
        table.setRedraw(true);
    }

    @Override
    protected void doUpdate(AnalysisContextVisualizationDistribution context) {

        // The statistics builder
        final StatisticsBuilderInterruptible builder = context.handle.getStatistics().getInterruptibleInstance();
        
        // Create an analysis
        Analysis analysis = new Analysis(){

            private boolean                      stopped = false;
            private StatisticsEquivalenceClasses summary;

            @Override
            public int getProgress() {
                return 0;
            }
            
            @Override
            public void onError() {
                setStatusEmpty();
            }

            @Override
            public void onFinish() {

                if (stopped) {
                    return;
                }

                // Now update the table
                table.setRedraw(false);
                table.removeAll();
                
                createItem(Resources.getMessage("EquivalenceClassStatistics.4"), //$NON-NLS-1$
                               format(summary.getAverageEquivalenceClassSizeIncludingOutliers(), summary.getNumberOfTuplesIncludingOutliers()),
                               format(summary.getAverageEquivalenceClassSize(), summary.getNumberOfTuples()));
                
                createItem(Resources.getMessage("EquivalenceClassStatistics.5"), //$NON-NLS-1$
                           format(summary.getMaximalEquivalenceClassSizeIncludingOutliers(), summary.getNumberOfTuplesIncludingOutliers()),
                           format(summary.getMaximalEquivalenceClassSize(), summary.getNumberOfTuples()));

                createItem(Resources.getMessage("EquivalenceClassStatistics.6"), //$NON-NLS-1$
                           format(summary.getMinimalEquivalenceClassSizeIncludingOutliers(), summary.getNumberOfTuplesIncludingOutliers()),
                           format(summary.getMinimalEquivalenceClassSize(), summary.getNumberOfTuples()));

                createItem(Resources.getMessage("EquivalenceClassStatistics.7"), //$NON-NLS-1$
                           format(summary.getNumberOfEquivalenceClassesIncludingOutliers()),
                           format(summary.getNumberOfEquivalenceClasses()));

                createItem(Resources.getMessage("EquivalenceClassStatistics.8"), //$NON-NLS-1$
                           format(summary.getNumberOfTuplesIncludingOutliers()),
                           format(summary.getNumberOfTuples(), summary.getNumberOfTuplesIncludingOutliers()));

                createItem(Resources.getMessage("EquivalenceClassStatistics.9"), //$NON-NLS-1$
                           format(summary.getNumberOfOutlyingTuples(), summary.getNumberOfTuplesIncludingOutliers()),
                           format(0));
                
                table.setRedraw(true);
                
                setStatusDone();
            }

            @Override
            public void onInterrupt() {
                setStatusWorking();
            }

            @Override
            public void run() throws InterruptedException {
                
                // Timestamp
                long time = System.currentTimeMillis();
                
                // Perform work
                this.summary = builder.getEquivalenceClassStatistics();

                // Our users are patient
                while (System.currentTimeMillis() - time < MINIMAL_WORKING_TIME && !stopped){
                    Thread.sleep(10);
                }
            }

            @Override
            public void stop() {
                builder.interrupt();
                this.stopped = true;
            }
        };
        
        this.manager.start(analysis);
    }
    
    /**
     * Formats the given string
     * @param value
     * @return
     */
    private String format(double value) {
        return format.format(value).replace(",", "."); 
    }

    /**
     * Formats the given string
     * @param value
     * @param baseline
     * @return
     */
    private String format(double value, double baseline) {
        StringBuilder builder = new StringBuilder();
        builder.append(format(value));
        builder.append(" (");
        builder.append(format(value / baseline * 100d));
        builder.append("%)");
        return builder.toString();
    }
    /**
     * Creates a table item
     * @param key
     * @param value1
     * @param value2
     */
    private void createItem(String key,
                            String value1,
                            String value2) {
        
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(0, key);
        item.setText(1, value1);
        if (super.getTarget() == ModelPart.OUTPUT) {
            item.setText(2, value2);
        }
    }
}