export default function Home() {
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-8">
      <main className="flex flex-col items-center gap-8 bg-white/30 backdrop-blur-md p-10 rounded-2xl shadow-xl border border-white/20">
        <h1 className="text-4xl font-bold text-gray-800">ChurchAI Admin</h1>
        <p className="text-gray-600 text-center max-w-md">
          Modern church management platform. Multi-tenant administration, member management, and advanced analytics.
        </p>
        <div className="flex gap-4">
          <button className="px-6 py-2 bg-blue-600 text-white rounded-full hover:bg-blue-700 transition">
            Login
          </button>
          <button className="px-6 py-2 bg-white text-blue-600 rounded-full border border-blue-600 hover:bg-blue-50 transition">
            Learn More
          </button>
        </div>
      </main>
    </div>
  );
}
