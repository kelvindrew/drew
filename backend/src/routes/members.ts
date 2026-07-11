import express from "express";
import { PrismaClient } from "@prisma/client";
import bcrypt from "bcrypt";

const router = express.Router();
const prisma = new PrismaClient();

// Register a new member
router.post("/", async (req, res) => {
  try {
    const { email, password, firstName, lastName, churchId } = req.body;

    // Check if user exists
    const existingUser = await prisma.user.findUnique({
      where: { email },
    });

    if (existingUser) {
      return res.status(400).json({ error: "User already exists" });
    }

    const saltRounds = 10;
    const hashedPassword = await bcrypt.hash(password, saltRounds);

    // Create the member
    const newUser = await prisma.user.create({
      data: {
        email,
        password: hashedPassword,
        firstName,
        lastName,
        churchId,
        role: "MEMBER"
      },
    });

    res.status(201).json(newUser);
  } catch (error) {
    console.error("Error creating member:", error);
    res.status(500).json({ error: "Internal server error" });
  }
});

// Get all members for a church
router.get("/church/:churchId", async (req, res) => {
  try {
    const { churchId } = req.params;

    const members = await prisma.user.findMany({
      where: { churchId },
      select: {
        id: true,
        email: true,
        firstName: true,
        lastName: true,
        role: true,
        campusId: true,
        createdAt: true,
      }
    });

    res.json(members);
  } catch (error) {
    res.status(500).json({ error: "Internal server error" });
  }
});

export default router;
