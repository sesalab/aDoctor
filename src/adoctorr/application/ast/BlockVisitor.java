package adoctorr.application.ast;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;

import java.util.ArrayList;

public class BlockVisitor extends ASTVisitor {

    private ArrayList<Block> blockList;

    BlockVisitor(ArrayList<Block> blockList) {
        this.blockList = blockList;
    }

    @Override
    public boolean visit(Block blockNode) {
        blockList.add(blockNode);
        return true;
    }
}
