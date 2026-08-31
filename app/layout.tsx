import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "DeleteMe | Digital Footprint Removal",
  description: "Find, track and request removal of exposed personal data across the public web, data brokers and breach intelligence sources.",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return <html lang="en"><body>{children}</body></html>;
}
