package SkewHeap;

public class SkewHeap {
    public SkewHeap(){}
    /* min node, top */
    public SkewNode root = null;


    public SkewNode merge(SkewNode h1, SkewNode h2){
        /* any one of the two is empty, return the non-empty one */
        if(h1 == null)
            return h2;
        if(h2 == null)
            return h1;

        /* before merge, we should adjust to make h1 the smaller one */
        if(h1.dst > h2.dst){
            /* swap the two */
            SkewNode tmp = h1;
            h1 = h2;
            h2 = tmp;
        }

        SkewNode tmp = merge(h1.right, h2);
        h1.right = h1.left;
        h1.left = tmp;
        /* relink the parent field */
        if(h1.right != null)
            h1.right.parent = h1;
        if(h1.left != null)
            h1.left.parent = h1;

        return h1;
    }

    /* insert a node through merge operation */
    public SkewNode insertNode(int id, int dst){
        /* l,r,p = null*/
        SkewNode node = new SkewNode(dst, id);
        this.root = merge(this.root, node);
        /* for record in shortest path map */
        return node;
    }

    /* remove the min one and merge its right and left children */
    public SkewNode getMin(){
        if(this.root == null)
            return null;
        /* back up, the returned node */
        SkewNode oroot = root;
        SkewNode lc = root.left;
        SkewNode rc = root.right;
        /* clear the parent field if any */
        if(lc != null)
            lc.parent = null;
        if(rc != null)
            rc.parent = null;
        /* reset the root */
        this.root = null;
        this.root = merge(lc, rc);
        return oroot;
    }

    /* update distance needed from Dijkstra */
    public void deceaseDst(SkewNode node, int ndst){
        node.dst = ndst;

        /* if root or parent key value is smaller
         * stay unmoved
         */
        if(node.parent == null)
            return;
        if(node.parent.dst <= ndst)
            return;

        /* break the min rule */
        SkewNode l = node.left;
        SkewNode r = node.right;

        /* clear parent field */
        if(l != null)
            l.parent = null;
        if(r != null)
            r.parent = null;

        /* merge left and right children */
        SkewNode tmp = merge(l, r);
        if (node.parent.left == node)
            node.parent.left = null;
        else if(node.parent.right == node)
            node.parent.right = null;

        /* merge the children to parent */
        node.parent = merge(node.parent, tmp);

        /* clear the node field */
        node = null;
    }

}
