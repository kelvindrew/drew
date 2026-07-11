import express from "express";
import cors from "cors";
import membersRouter from "./routes/members.js";

const app = express();
app.use(cors());
app.use(express.json());

app.use("/api/members", membersRouter);

app.get("/", (req, res) => {
  res.json({ message: "ChurchAI API Running" });
});

const PORT = process.env.PORT || 4000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
